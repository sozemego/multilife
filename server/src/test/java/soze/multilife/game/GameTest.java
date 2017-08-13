package soze.multilife.game;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameTest {

	private static final int ID = 1;
	private static final float DEFAULT_INITIAL_DENSITY = 0.0f;
	private static final int DEFAULT_WIDTH = 50;
	private static final int DEFAULT_HEIGHT = 50;
	private static final int DEFAULT_MAX_PLAYERS = 4;
	private static final long DEFAULT_DURATION = 1000 * 1;

	protected BaseGame createGameWithDefaultValues() {
		return builder().build();
	}

	protected BaseGameBuilder builder() {
		return new BaseGameBuilder();
	}

	protected Player createPlayerMock(long id) {
		Player player = mock(Player.class);
		when(player.getRule()).thenReturn("BASIC");
		when(player.getId()).thenReturn(id);
		doNothing().when(player).disconnect();
		return player;
	}

	protected GameIncomingMessageQueue getGameIncomingMessageQueue(Game game) {
		return new GameIncomingMessageQueue(game);
	}

	protected GameOutgoingMessageHandler getGameOutgoingMessageHandler(Game game) {
		return new GameOutgoingMessageHandler(game);
	}

	protected static class BaseGameBuilder {

		private int id = ID;
		private float initialDensity = DEFAULT_INITIAL_DENSITY;
		private int width = DEFAULT_WIDTH;
		private int height = DEFAULT_HEIGHT;
		private int maxPlayers = DEFAULT_MAX_PLAYERS;
		private long duration = DEFAULT_DURATION;

		protected BaseGameBuilder() {

		}

		BaseGameBuilder withMaxPlayers(int maxPlayers) {
			this.maxPlayers = maxPlayers;
			return this;
		}

		BaseGameBuilder withInitialDensity(float initialDensity) {
			this.initialDensity = initialDensity;
			return this;
		}

		BaseGameBuilder withWidth(int width) {
			this.width = width;
			return this;
		}

		BaseGameBuilder withHeight(int height) {
			this.height = height;
			return this;
		}

		BaseGameBuilder withDuration(long duration) {
			this.duration = duration;
			return this;
		}

		BaseGame build() {
			return new BaseGame(id, initialDensity, width, height, maxPlayers, duration);
		}

	}



}
