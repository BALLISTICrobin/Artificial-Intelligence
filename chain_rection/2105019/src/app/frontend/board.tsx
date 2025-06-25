import { gamestate, move } from "../backend/game";
import Cell from "./cell";

interface BoardProps {
  state: gamestate;
  onMove: (move: move) => void;
}

export default function Board({ state, onMove }: BoardProps) {
  return (
    <div className="flex justify-center items-center p-6">
      <div className="card bg-base-100 shadow-2xl border border-base-300">
        <div className="card-body p-6">
          <div className="grid grid-cols-6 gap-3 p-4 bg-gradient-to-br from-base-200 to-base-300 rounded-2xl shadow-inner">
            {state.board.map((row, r) =>
              row.map((cell, c) => (
                <Cell
                  key={`${r}-${c}`}
                  cell={cell}
                  onClick={() => onMove({ row: r, col: c, player: 'red' })}
                />
              ))
            )}
          </div>
          
          {/* Board decorations */}
          <div className="flex justify-between items-center mt-4 text-sm opacity-60">
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 bg-red-500 rounded-full"></div>
              <span>Your pieces</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
              <span>AI pieces</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}