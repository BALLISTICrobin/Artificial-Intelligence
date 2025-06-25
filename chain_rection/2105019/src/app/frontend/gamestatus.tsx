import { gamestate } from "../backend/game";

interface GameStatusProps {
  state: gamestate;
  flag: boolean;
}

export default function GameStatus({ state,flag }: GameStatusProps) {
  const getPlayerBadge = (player: string) => {
    if(flag)
    {
      return <div className="badge badge-info badge-lg">🤖 AI</div>;
    }
    if (player === 'red') {
      return <div className="badge badge-error badge-lg">🔴 You</div>;
    } else {
      return <div className="badge badge-info badge-lg">🤖 AI</div>;
    }
  };

  const getTotalOrbs = (player: 'red' | 'blue') => {
    let num_of_orbs = 0;
    for (const row of state.board) {
      for (const cell of row) {
        if (cell.player === player) {
          num_of_orbs += cell.orb_count;
        }
      }
    }
    return num_of_orbs; 
  };

  return (
    <div className="flex flex-col items-center gap-4 mb-6">
      {/* Game Status Card */}
      <div className="card bg-base-100 shadow-xl border border-base-300 w-full max-w-md">
        <div className="card-body text-center p-4">
          {state.winner !== 'blank' ? (
            <div className="space-y-3">
              <div className="text-4xl">
                { state.winner === 'red' ? '🔴' : '🔵'}
              </div>
              <h2 className={`text-2xl font-bold ${
                state.winner === 'red' ? 'text-success' : 'text-info'
              }`}>
                {flag ? 'Victory' : state.winner === 'red' ? 'Victory!' : 'AI Wins!'}
              </h2>
              <p className="text-base-content/70">
                {flag ? (state.winner === 'red' ? 'Red AI defeated Blue AI!' : 'Blue AI defeated Red AI!') : (state.winner === 'red' ? 'Congratulations! You defeated the AI!' : 'Better luck next time!')}
              </p>
              <div className="card-actions justify-center">
                <button className="btn btn-primary btn-sm" onClick={() => window.location.reload()}>
                  Play Again
                </button>
              </div>
            </div>
          ) : (
            <div className="space-y-3">
              <div className="text-2xl">
                {state.current_player === 'red' ? '🎯' : '🤔'}
              </div>
              <h2 className="text-xl font-semibold">
                Current Turn
              </h2>
              <div className="flex justify-center">
                {getPlayerBadge(state.current_player)}
              </div>
              {state.current_player === 'red' ? (
                <p className="text-sm text-base-content/70">Click on any empty cell or your own pieces</p>
              ) : (
                <div className="flex items-center justify-center gap-2 text-sm text-base-content/70">
                  <span className="loading loading-dots loading-sm"></span>
                  AI is thinking...
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Game Statistics */}
      <div className="stats shadow border border-base-300">
        <div className="stat place-items-center">
          <div className="stat-title">Your Orbs</div>
          <div className="stat-value text-error">{getTotalOrbs('red')}</div>
          <div className="stat-desc">Total count</div>
        </div>
        
        <div className="stat place-items-center">
          <div className="stat-title">AI Orbs</div>
          <div className="stat-value text-info">{getTotalOrbs('blue')}</div>
          <div className="stat-desc">Total count</div>
        </div>
      </div>
    </div>
  );
}