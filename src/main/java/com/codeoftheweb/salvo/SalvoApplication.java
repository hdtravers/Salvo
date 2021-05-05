package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootApplication
public class SalvoApplication {

	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}


	@Bean
	public CommandLineRunner initData(PlayerRepository repository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ScoreRepository scoreRepository) {
		return (args) -> {

			// save a couple of customers
			Player player1 = new Player("j.bauer@ctu.gov", passwordEncoder.encode("24"));
			Player player2 = new Player("c.obrian@ctu.gov", passwordEncoder.encode("42"));
			Player player3 = new Player("kim_bauer@gmail.com", passwordEncoder.encode("kb"));
			Player player4 = new Player("t.almeida@ctu.gov", passwordEncoder.encode("mole"));
			Player player5 = new Player("d.palmer@whitehouse.gov",passwordEncoder.encode( "fuck"));


			Game game1 = new Game(LocalDateTime.now());
			Game game2 = new Game(LocalDateTime.now().plusHours(1));
			Game game3 = new Game(LocalDateTime.now().plusHours(2));



			repository.save(player1);
			repository.save(player2);
			repository.save(player3);
			repository.save(player4);
			repository.save(player5);
			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);



			Score score1 = new Score(player1, game1, 0.5);
			Score score2 = new Score(player2, game1, 1);
			Score score3 = new Score(player3, game2, 0.5);
			Score score4 = new Score(player4, game2, 0.5);


			scoreRepository.save(score1);
			scoreRepository.save(score2);
			scoreRepository.save(score3);
			scoreRepository.save(score4);

			GamePlayer gamePlayer1 = new GamePlayer(game1, player1);

			GamePlayer gamePlayer2 = new GamePlayer(game1, player2);

			GamePlayer gamePlayer3 = new GamePlayer(game2, player3);

			GamePlayer gamePlayer4 = new GamePlayer(game2, player4);

			GamePlayer gamePlayer5 = new GamePlayer(game3, player4);

			Ship ship1 = new Ship("Destructor", Arrays.asList("A2", "A3", "A4"));
			Ship ship2 = new Ship("Portaaviones", Arrays.asList("D2", "D3", "D4", "D5", "D6"));
			Ship ship3 = new Ship("Bote de Patrulla", Arrays.asList("B9", "B10"));
			Ship ship4 = new Ship("Acorazado", Arrays.asList("C2", "C3", "C4", "C5"));
			Ship ship5 = new Ship("Submarino", Arrays.asList("E2", "E3", "E4"));
			Ship ship6 = new Ship("Destructor", Arrays.asList("A3", "A4", "A5"));
			Ship ship7 = new Ship("Portaaviones", Arrays.asList("D3", "D4", "D5", "D6", "D7"));
			Ship ship8 = new Ship("Bote de Patrulla", Arrays.asList("B10", "B11"));
			Ship ship9 = new Ship("Acorazado", Arrays.asList("C3", "C4", "C5", "C6"));
			Ship ship10 = new Ship("Submarino", Arrays.asList("E3", "E4", "E5"));

			Salvo salvo1 = new Salvo(Arrays.asList("H1", "A1", "A10"), 1);
			Salvo salvo2 = new Salvo(Arrays.asList("H2", "A2", "A9"), 2);
			Salvo salvo3 = new Salvo(Arrays.asList("H3", "A3", "A8"), 3);
			Salvo salvo4 = new Salvo(Arrays.asList("H4", "A4", "A7"), 4);
			Salvo salvo5 = new Salvo(Arrays.asList("H5", "A5", "A6"), 5);
			Salvo salvo6 = new Salvo(Arrays.asList("H6", "A6", "A4"), 6);
			Salvo salvo7 = new Salvo(Arrays.asList("H7", "A7", "A3"), 7);
			Salvo salvo8 = new Salvo(Arrays.asList("H8", "A8", "A2"), 8);


			gamePlayer1.addShip(ship1);
			gamePlayer1.addShip(ship2);
			gamePlayer1.addShip(ship3);
			gamePlayer1.addShip(ship4);
			gamePlayer1.addShip(ship5);

			gamePlayer2.addShip(ship6);
			gamePlayer2.addShip(ship7);
			gamePlayer2.addShip(ship8);
			gamePlayer2.addShip(ship9);
			gamePlayer2.addShip(ship10);


			gamePlayer1.addSalvo(salvo1);
			gamePlayer1.addSalvo(salvo2);
			gamePlayer2.addSalvo(salvo3);
			gamePlayer2.addSalvo(salvo4);

			gamePlayer3.addSalvo(salvo5);
			gamePlayer3.addSalvo(salvo6);
			gamePlayer4.addSalvo(salvo7);
			gamePlayer4.addSalvo(salvo8);


			gamePlayerRepository.save(gamePlayer1);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);
			gamePlayerRepository.save(gamePlayer5);

		};
	}
}
		@Configuration
		class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {
			@Autowired
			PlayerRepository playerRepository;

			@Bean
			public PasswordEncoder passwordEncoder() {
				return PasswordEncoderFactories.createDelegatingPasswordEncoder();
			}

			@Override
			public void init(AuthenticationManagerBuilder auth) throws Exception {
				auth.userDetailsService(inputPlayer-> {
					Player player = playerRepository.findByUserName(inputPlayer);
					if (player != null) {
						return new User(player.getUserName(), player.getPassword(),
								AuthorityUtils.createAuthorityList("USER"));
					} else {
						throw new UsernameNotFoundException("Unknown user: " + inputPlayer);
					}

				});
			}
		}
@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/web/game","/api/game_view/**").hasAuthority("USER")
				.antMatchers("/**").permitAll()
				.and()
				.formLogin();

		http.formLogin()
				.usernameParameter("userName")
				.passwordParameter("password")
				.loginPage("/api/login");

		http.logout().logoutUrl("/api/logout");

		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}



	}




