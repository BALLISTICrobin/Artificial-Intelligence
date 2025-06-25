//types of variables for chain-reaction game(minimax, alpha-beta pruning)
export type player = 'red'|'blue'| 'blank';

export type cell ={
    rowIndex: number;
    colIndex: number;
    orb_count: number;
    player: player;
}

export type board = cell[][];

export type move ={
    row: number;
    col: number;
    player: player;
}

export type gamestate ={
    board: board;
    current_player: player;
    winner: player ;
}
