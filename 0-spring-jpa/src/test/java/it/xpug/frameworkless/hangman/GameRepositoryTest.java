package it.xpug.frameworkless.hangman;

import it.xpug.frameworkless.hangman.db.GameRepository;
import it.xpug.frameworkless.hangman.domain.Game;
import it.xpug.frameworkless.hangman.domain.GameIdGenerator;
import it.xpug.frameworkless.hangman.domain.Prisoner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.math.BigInteger;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@TestPropertySource("classpath:application-test.properties")
public class GameRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @MockBean
    GameIdGenerator gameIdGenerator;

    @Autowired
    GameRepository gameRepository;

    @Before
    public void setUp() {
        entityManager.createNativeQuery("delete from hangman_games").executeUpdate();
    }

    @Test
    public void createNewGame() throws Exception {
        when(gameIdGenerator.generateGameId()).thenReturn(123L);
        assertThat(gameCount(), is(0));

        Game game = gameRepository.createNewGame();

        assertThat(game.getGameId(), is(123L));
        assertThat(gameCount(), is(1));
    }

    @Test
    public void findGame() throws Exception {
        when(gameIdGenerator.generateGameId()).thenReturn(789L);
        gameRepository.createNewGame();

        Optional<Game> game = gameRepository.findGame(789L);

        assertThat("not present", game.isPresent(), is(true));
        assertThat(game.get().getGameId(), is(789L));
        assertThat(game.get().getPrisoner().getGuessesRemaining(), is(18));
    }

    @Test
    public void saveAndLoad() throws Exception {
        Game original = new Game(42L, new Prisoner("foobar"));
        original.getPrisoner().guess("x");
        original.getPrisoner().guess("y");
        original.getPrisoner().guess("f");
        original.getPrisoner().guess("o");
        gameRepository.save(original);

        Optional<Game> game = gameRepository.findGame(42L);

        assertThat("not present", game.isPresent(), is(true));
        assertThat(game.get().getPrisoner(), is(original.getPrisoner()));
    }

    @Test
    public void gameNotFound() throws Exception {
        Optional<Game> game = gameRepository.findGame(9869L);

        assertThat("not present", game.isPresent(), is(false));
    }

    private int gameCount() {
        String sql = "select count(*) from hangman_games";
        return Integer.valueOf(((BigInteger) select(sql)).toString());
    }

    private Object select(String sql) {
        return entityManager.createNativeQuery(sql).getSingleResult();
    }


}