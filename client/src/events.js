// fired after player placed a shape on the game board
export const SHAPE_PLACED = "SHAPE_PLACED";

// fired when player clicked on the game board to place a shape
export const PLACE_SHAPE = "PLACE_SHAPE";

// fired when any of the shapes are selected in game-ui
export const SHAPE_SELECTED = "SHAPE_SELECTED";

// fired when a player attempts to login
export const LOGIN = "LOGIN";

// fired when player successfully logged in
export const LOGGED_IN = "LOGGED_IN";

// fired when player data updates
export const PLAYER_DATA_UPDATED = "PLAYER_DATA_UPDATED";

// fired when server sends a list of cells
export const CELL_LIST = "CELL_LIST";

// fired when server sends current player id
export const PLAYER_IDENTITY = "PLAYER_IDENTITY";

// fired when server sends data about the map (width, height)
export const MAP_DATA = "MAP_DATA";

// fired when server sends the go ahead for a new simulation tick
export const TICK_DATA = "TICK_DATA";

// fired when server sends message about player data
// (points, colors, etc.).
// This message should not be received from server anymore
// since it was broken into more granular messages
export const PLAYER_DATA = "PLAYER_DATA";

// fired when server sends data about time til game ends
export const TIME_REMAINING = "TIME_REMAINING";

// fired when a new player joins the game
export const PLAYER_ADDED = "PLAYER_ADDED";

// fired when a player leaves the game
export const PLAYER_REMOVED = "PLAYER_REMOVED";

// fired when a data about player points is received
export const PLAYER_POINTS = "PLAYER_POINTS";