import { NextResponse } from 'next/server';
import { readGameState, writeGameState } from '../../backend/filehandling';
import { applyMove, getLegalMoves } from '../../backend/gameengine';
import { minimaxSearch } from '../../backend/gameengine';
import path from 'path';

export async function POST() {
  try {
    console.log('AI move request received');
    const filePath = path.join(process.cwd(), 'gamestate.txt');

    const { header, state } = await readGameState(filePath, true);

    console.log('Current game header:', header);
    if (header !== 'AI1 Move:' && header !== 'AI2 Move:') {
      return NextResponse.json({ error: 'Not AI\'s turn' }, { status: 400 });
    }

    const TIMEOUT_MS = 5000; // 5 seconds timeout
    
    console.log('Starting minimax search with timeout...');
    const aiPlayer = header === 'AI1 Move:' ? 'red' : 'blue';
   
    const [, bestMove] = minimaxSearch(state, 3, aiPlayer, TIMEOUT_MS);
    console.log('Best move found:', bestMove);

    if (!bestMove) {
      return NextResponse.json({ error: 'No valid moves' }, { status: 400 });
    }

    const nextHeader = header === 'AI1 Move:' ? 'AI2 Move:' : 'AI1 Move:';
    const newState = applyMove(state, bestMove);
    await writeGameState('gamestate.txt', nextHeader, newState);
    
    console.log('AI move applied:', bestMove);
    console.log('player after AI move:', newState.current_player);
    
    return NextResponse.json(newState, { status: 200 });
  } catch (error) {
    console.error('Server error:', error);
    return NextResponse.json({ error: 'Server error' }, { status: 500 });
  }
}