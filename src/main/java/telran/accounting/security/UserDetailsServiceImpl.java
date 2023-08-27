package telran.accounting.security;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import telran.accounting.configuration.EmailEncryptionConfiguration;
import telran.accounting.dao.ProfileRepository;
import telran.accounting.model.Profile;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final ProfileRepository profileRepository;
    private final JwtTokenService jwtTokenService;

    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String encryptedEmail = EmailEncryptionConfiguration.encryptAndEncodeUserId(email);
        Profile profile = profileRepository.findById(encryptedEmail).orElseThrow(()-> new UsernameNotFoundException(encryptedEmail));
        String[] roles = profile.getRoles()
                .stream()
                .map(r-> "ROLE_" + r)
                .toArray(String[]::new);
        jwtTokenService.generateToken(profile);
        return new User(profile.getEmail(), profile.getPassword(), AuthorityUtils.createAuthorityList(roles));
    }
}
