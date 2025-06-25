import { board, gamestate, player } from "./game";
import { getCriticalMass } from "./gameengine";

// Heuristic 1: Orb count difference
function heuristicOrbCount(state: gamestate,aiplayer: player): number {
    let redOrbs = 0;
    let blueOrbs = 0;
    for (const row of state.board) {
      for (const cell of row) {
        if (cell.player === 'red') redOrbs += cell.orb_count;
        if (cell.player === 'blue') blueOrbs += cell.orb_count;
      }
    }
    if( aiplayer === 'red') {
      return redOrbs - blueOrbs; // Red AI's perspective
    }
    else {
      return blueOrbs - redOrbs; // Blue AI's perspective
    }
  }

  // Heuristic 2: Control of critical cells (cells close to critical mass)
function heuristicCriticalControl(state: gamestate, aiPlayer: player): number {
    let score = 0;
    for (let r = 0; r < state.board.length; r++) {
      for (let c = 0; c < state.board[0].length; c++) {
        const cell = state.board[r][c];
        if (cell.player === aiPlayer && cell.orb_count >= getCriticalMass(cell, state.board) - 1) {
          score += 10;
        } else if (cell.player === (aiPlayer === 'red' ? 'blue' : 'red') && cell.orb_count >= getCriticalMass(cell, state.board) - 1) {
          score -= 10;
        }
      }
    }
    return score;
  }

  // Heuristic 3: Board control (number of cells occupied)
function heuristicBoardControl(state: gamestate, aiplayer:player): number {
    let blueCells = 0;
    let redCells = 0;
    for (const row of state.board) {
      for (const cell of row) {
        if (cell.player === 'blue') blueCells++;
        if (cell.player === 'red') redCells++;
      }
    }
    if (aiplayer === 'red') {
      return redCells - blueCells; // Red AI's perspective
    }
    else {
      return blueCells - redCells; // Blue AI's perspective
    }
  }

  // Heuristic 4: Potential explosion chain length
function heuristicExplosionChain(state: gamestate, aiPlayer: player): number {
    let score = 0;
    for (let r = 0; r < state.board.length; r++) {
      for (let c = 0; c < state.board[0].length; c++) {
        if (state.board[r][c].player === aiPlayer && state.board[r][c].orb_count >= getCriticalMass(state.board[r][c], state.board) - 1) {
          // Simulate explosion to estimate chain length
          score += simulateExplosionChain(state.board, r, c, aiPlayer);
        }
      }
    }
    return score;
  }

  function simulateExplosionChain(originalBoard: board, row: number, col: number, player: player): number {
    const board = JSON.parse(JSON.stringify(originalBoard)) as board; // Deep copy
    let chainLength = 0;
    const queue: [number, number][] = [[row, col]];
  
    // Add one orb to simulate the triggering move
    board[row][col].orb_count += 1;
    board[row][col].player = player;
  
    while (queue.length > 0) {
      const [r, c] = queue.shift()!;
      const critical = getCriticalMass(board[r][c],board);
  
      if (board[r][c].orb_count >= critical) {
        chainLength++;
        board[r][c].orb_count -= critical;
  
        // If the cell is emptied, reset player
        if (board[r][c].orb_count === 0) {
          board[r][c].player = 'blank';
        }
  
        const directions = [[0, 1], [0, -1], [1, 0], [-1, 0]];
        for (const [dr, dc] of directions) {
          const nr = r + dr;
          const nc = c + dc;
          if (nr >= 0 && nr < board.length && nc >= 0 && nc < board[0].length) {
            board[nr][nc].orb_count += 1;
            board[nr][nc].player = player;
  
            // If this causes another explosion, enqueue it
            if (board[nr][nc].orb_count >= getCriticalMass(board[nr][nc], board)) {
              queue.push([nr, nc]);
            }
          }
        }
      }
      if(chainLength >= 100) {
        // Prevent infinite loops in case of very long chains
        return chainLength;
      }
    }
  
    return chainLength;
  }

  // Heuristic 5: Positional Safety & Threat Exposure

function heuristicPositionalSafety(state: gamestate, aiPlayer: player): number {
  const opponent = aiPlayer === 'red' ? 'blue' : 'red';
  let score = 0;

  for (let r = 0; r < state.board.length; r++) {
    for (let c = 0; c < state.board[0].length; c++) {
      const cell = state.board[r][c];

      // Only evaluate our own cells
      if (cell.player !== aiPlayer) continue;

      const critical = getCriticalMass(cell, state.board);
      const distanceToExplosion = critical - cell.orb_count;

      // Add positional safety bonus (corner and edge preference)
      let positionalBonus = 0;
      const isCorner = (r === 0 || r === state.board.length - 1) && (c === 0 || c === state.board[0].length - 1);
      const isEdge = r === 0 || r === state.board.length - 1 || c === 0 || c === state.board[0].length - 1;
      if (isCorner) positionalBonus += 5;
      else if (isEdge) positionalBonus += 3;

      // Check threat from adjacent opponent cells
      let threatPenalty = 0;
      const directions = [[0, 1], [0, -1], [1, 0], [-1, 0]];

      for (const [dr, dc] of directions) {
        const nr = r + dr;
        const nc = c + dc;

        if (nr >= 0 && nr < state.board.length && nc >= 0 && nc < state.board[0].length) {
          const neighbor = state.board[nr][nc];

          if (neighbor.player === opponent) {
            const neighborCritical = getCriticalMass(neighbor, state.board);
            const neighborThreatLevel = neighbor.orb_count / neighborCritical;

            // Penalize more if the enemy is closer to explosion
            if (neighbor.orb_count >= neighborCritical - 1) {
              threatPenalty += 10; // Immediate danger
            } else {
              threatPenalty += Math.floor(5 * neighborThreatLevel); // Gradual threat
            }
          }
        }
      }

      // Vulnerable cells (1 orb from explosion) get extra penalty
      if (distanceToExplosion === 1) {
        threatPenalty += 3;
      }

      // Final score from this cell
      score += positionalBonus - threatPenalty;
    }
  }

  return score;
}

export function even_priority_heuristic(state: gamestate, aiPlayer: player): number {
  
  let score = 0;
  for (let r = 0; r < state.board.length; r++) {
    for (let c = 0; c < state.board[0].length; c++) {
      const cell = state.board[r][c];

      if(cell.player==aiPlayer){
        let sum_of_row_col = r+c;
        if(sum_of_row_col%2==0){
          
          score+=sum_of_row_col;
          score += r;
        }
        else{
          score+=0;
        }
        
      }
    }}
    return score;

}


export function evaluate(state: gamestate, aiPlayer: player): number {
  if (state.winner !== 'blank') {
    return state.winner === aiPlayer ? Infinity : -Infinity;
  }

  const orbScore = heuristicOrbCount(state,aiPlayer);                  
  const criticalScore = heuristicCriticalControl(state, aiPlayer);     
  const controlScore = heuristicBoardControl(state,aiPlayer);
  // console.log('inside chain rection heuristics');         
  const chainScore = heuristicExplosionChain(state, aiPlayer);
  // console.log('chain score:', chainScore);         
  const safetyScore = heuristicPositionalSafety(state, aiPlayer);      

  // Dynamic weight adjustment based on game phase
  const totalOrbs = orbScore + Math.abs(orbScore); 
  let gamePhaseMultiplier = 1;
  if (totalOrbs < 10) gamePhaseMultiplier = 0.6;  // early
  else if (totalOrbs < 30) gamePhaseMultiplier = 1.0; // mid
  else gamePhaseMultiplier = 1.5;  // late

  return (
    orbScore * 0.5 +
    criticalScore * 2.0 +
    controlScore * 1.0 +
    chainScore * 3.0 * gamePhaseMultiplier +
    safetyScore * 1.0
  );
}
  
  


  