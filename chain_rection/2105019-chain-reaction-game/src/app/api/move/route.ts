import { NextResponse } from 'next/server';
import { readGameState,writeGameState } from '../../backend/filehandling';
import { applyMove } from '../../backend/gameengine';
import { minimaxSearch } from '../../backend/gameengine';// Adjust if minimax is in gameengine.tsx
import path from 'path';

export async function POST() {
  try {
    console.log('AI move request received');
    const filePath = path.join(process.cwd(), 'gamestate.txt'); // Absolute path

    const { header, state } = await readGameState(filePath,false);
    console.log('Current game header:', header);
    if (header !== 'Human Move:') {
      return NextResponse.json({ error: 'Not AI’s turn' }, { status: 400 });
    }
    console.time('AI move');
    const TIMEOUT_MS = 6000; // 5 seconds timeout
    
    console.log('Starting minimax search with timeout...');
    
    // Call minimax with built-in timeout support
    const [, bestMove] = minimaxSearch(state, 3, 'blue', TIMEOUT_MS); // Assuming AI is always blue
    console.timeEnd('AI move');
    if (!bestMove) {
      return NextResponse.json({ error: 'No valid moves' }, { status: 400 });
    }
    const newState = applyMove(state, bestMove);
    await writeGameState(filePath, 'AI Move:', newState);
    return NextResponse.json(newState, { status: 200 });
  } catch{
    return NextResponse.json({ error: 'Server error' }, { status: 500 });
  }
}