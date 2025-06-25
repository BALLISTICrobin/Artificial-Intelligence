import { cell as cellType} from "../backend/game";

interface CellProps {
  cell: cellType;
  onClick: () => void;
}

export default function Cell({ cell, onClick }: CellProps) {
  const getCellStyle = () => {
    if (cell.player === 'red') {
      return 'bg-gradient-to-br from-red-400 to-red-600 border-red-300 shadow-red-200';
    } else if (cell.player === 'blue') {
      return 'bg-gradient-to-br from-blue-400 to-blue-600 border-blue-300 shadow-blue-200';
    } else {
      return 'bg-gradient-to-br from-gray-50 to-gray-100 border-gray-300 hover:from-gray-100 hover:to-gray-200';
    }
  };

  const getOrbAnimation = () => {
    if (cell.orb_count >= 3) {
      return 'animate-pulse';
    } else if (cell.orb_count >= 2) {
      return 'animate-bounce';
    }
    return '';
  };

  const isClickable = cell.player === 'blank' || cell.player === 'red';

  return (
    <button
      className={`
        w-16 h-16 ${getCellStyle()} 
        border-2 flex items-center justify-center 
        text-white font-bold rounded-xl 
        shadow-lg hover:shadow-xl
        transition-all duration-300 ease-in-out
        ${isClickable ? 'hover:scale-105 cursor-pointer' : 'cursor-not-allowed opacity-75'}
        ${getOrbAnimation()}
        relative overflow-hidden
      `}
      onClick={onClick}
      disabled={!isClickable}
    >
      {/* Orb visualization */}
      {cell.orb_count > 0 && (
        <div className="relative">
          {/* Single orb */}
          {cell.orb_count >= 1 && (
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="w-3 h-3 bg-white rounded-full shadow-inner opacity-90"></div>
            </div>
          )}
          
          {/* Two orbs */}
          {cell.orb_count >= 2 && (
            <>
              <div className="absolute -top-1 -left-1">
                <div className="w-2.5 h-2.5 bg-white rounded-full shadow-inner opacity-80"></div>
              </div>
              <div className="absolute -bottom-1 -right-1">
                <div className="w-2.5 h-2.5 bg-white rounded-full shadow-inner opacity-80"></div>
              </div>
            </>
          )}
          
          {/* Three orbs */}
          {cell.orb_count >= 3 && (
            <>
              <div className="absolute -top-1 right-0">
                <div className="w-2 h-2 bg-white rounded-full shadow-inner opacity-70"></div>
              </div>
              <div className="absolute -bottom-1 left-0">
                <div className="w-2 h-2 bg-white rounded-full shadow-inner opacity-70"></div>
              </div>
            </>
          )}
          
          {/* Critical mass indicator */}
          {cell.orb_count >= 4 && (
            <div className="absolute inset-0 border-2 border-white rounded-full animate-ping opacity-50"></div>
          )}
        </div>
      )}
      
      {/* Orb count number */}
      <span className="absolute bottom-0 right-1 text-xs font-bold drop-shadow-sm">
        {cell.orb_count > 0 ? cell.orb_count : ''}
      </span>
      
      {/* Hover effect overlay */}
      {isClickable && (
        <div className="absolute inset-0 bg-white opacity-0 hover:opacity-10 transition-opacity duration-200 rounded-xl"></div>
      )}
    </button>
  );
}