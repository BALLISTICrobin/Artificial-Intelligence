'use server';

import fs from 'fs/promises';

import { board,gamestate,player } from './game';

export async function readGameState(filePath: string, flag: boolean): Promise<{ header: string; state: gamestate }> {
    const content = await fs.readFile(filePath, 'utf-8');
    const lines = content.trim().split('\n');
    const header = lines[0];
  
    const board: board = lines.slice(1, 10).map((row, rowIndex) =>
      row.split(' ').map((cell, colIndex) => {
        if (cell === '0') return { orb_count: 0, player: 'blank', rowIndex, colIndex };
        const orb_count = parseInt(cell.slice(0, -1));
        const color = cell.slice(-1) as 'R' | 'B';
        const player = color === 'R' ? 'red' : 'blue';
        return { orb_count, player, rowIndex, colIndex };
      })
    );
  
    let currentPlayer:player = 'blank';

    if(flag){
      currentPlayer = header === 'AI1 Move:' ? 'blue' : 'red';
    }
    else{
      currentPlayer = header === 'Human Move:' ? 'blue' : 'red';
    }
  
    return {
      header,
      state: {
        board,
        current_player: currentPlayer,
        winner: 'blank'
      }
    };
  }
  
  
  export async function writeGameState(filePath: string, header: string, state: gamestate): Promise<void> {
    const lines: string[] = [header];
  
    for (const row of state.board) {
      const rowStr = row
        .map(cell => {
          if (cell.orb_count === 0 || cell.player === 'blank') {
            return '0';
          } else {
            const colorChar = cell.player === 'red' ? 'R' : 'B';
            return `${cell.orb_count}${colorChar}`;
          }
        })
        .join(' ');
      lines.push(rowStr);
    }
  
    await fs.writeFile(filePath, lines.join('\n'), 'utf-8');
  }