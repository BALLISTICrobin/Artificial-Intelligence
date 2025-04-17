from a_star_algo import AStarAlgo
Hamming_Distance =1
Manhattan_Distance=2
Euclidean_Distance =3
Linear_Conflict =4

def read_board(size):
   
    initial_board = []
    print(f"populate the board with numbers from {1} to {size**2-1}, here 0 defines blank" )
    
    for _ in range(size):
        while True:
            row_input = input().strip()
            values = row_input.split()
            
            # Validate input
            if len(values) != size:
                print(f"Error: Expected {size} values per row. Try again.")
                continue
                
            try:
                row = list(map(int, values))
                initial_board.append(row)
                break
            except ValueError:
                print("Error: Please enter only numbers. Try again.")
    
    return initial_board

def create_goal_board(size):
    """Create the goal board configuration for the given size"""
    goal = []
    num = 1
    for i in range(size):
        row = []
        for j in range(size):
            if i == size-1 and j == size-1:
                row.append(0)
            else:
                row.append(num)
                num += 1
        goal.append(row)
    return goal

def print_solution(solution_path, solver):
    """Print the solution in the required format"""
    if solution_path:
        print(f"Minimum number of moves = {len(solution_path)-1}")
        print("\nSteps:")
        for node in solution_path:
            for i in range(len(node.board)):
                for j in range(len(node.board[i])):
                    if node.board[i][j] == 0:
                        print(" ", end=" ")
                    else:
                        print(node.board[i][j], end=" ")
                print()
            print()
    else:
        print("Unsolvable puzzle")
    
    # print(f"Nodes explored: {solver.explored_nodes}")
    # print(f"Nodes expanded: {solver.expanded_nodes}")

def is_solvable(board):
    """
    Check if the given board configuration is solvable.
    Returns True if solvable, False otherwise.
    """
    n = len(board)
    inversions = 0
    blank_row = 0
    
    # Flatten the board and count inversions
    flat_board = []
    for i in range(n):
        for j in range(n):
            if board[i][j] == 0:
                blank_row = n - i  # row from bottom (1-based)
            else:
                flat_board.append(board[i][j])
    
    # Count inversions
    for i in range(len(flat_board)):
        for j in range(i + 1, len(flat_board)):
            if flat_board[i] > flat_board[j]:
                inversions += 1
    
    # Check solvability rules
    if n % 2 == 1:  # Odd grid size
        return inversions % 2 == 0
    else:  # Even grid size
        return ((blank_row % 2 == 1) and (inversions % 2 == 0)) or ((blank_row % 2 == 0) and (inversions % 2 == 1))

def main():
    print("N-Puzzle Solver using A* Algorithm")
    print("Enter the size(3/4/5... ... ...)")
    size = int(input())
    initial_board = read_board(size)
    goal_board = create_goal_board(size)
    
    # Check if puzzle is solvable
    if not is_solvable(initial_board):
        print("Unsolvable puzzle")
        return
    
    # Select heuristic
    print("\nSelect heuristic function:")
    print("1. Hamming Distance")
    print("2. Manhattan Distance")
    print("3. Euclidean Distance")
    print("4. Linear Conflict")
    choice = int(input("Enter choice (1-4): "))
    
    heuristic_map = {
        1: Hamming_Distance,
        2: Manhattan_Distance,
        3: Euclidean_Distance,
        4: Linear_Conflict
    }
    
    if choice not in heuristic_map.keys():
        print("Invalid choice. Using Manhattan Distance as default.")
        choice = 2
    
    # Solve the puzzle
    n_puzzle_solver = AStarAlgo(heuristic_map[choice])
    solution_path = n_puzzle_solver.solve(initial_board, goal_board)
    
    # Print results
    print_solution(solution_path, n_puzzle_solver)

if __name__ == "__main__":
    main()