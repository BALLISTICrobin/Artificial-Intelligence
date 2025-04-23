import copy 

class Node:
    def __init__(self, board, parent=None, action=None):
        self.board = board  # The current state of the puzzle
        self.parent = parent  # The parent node (previous state)
        self.action = None  # The action taken to reach this state
        self.g = 0  # Cost from the start node to this node
        self.h = 0  # Heuristic cost from this node to the goal
        self.f = 0  # Total cost (g + h)
        
    def getBlankPosition(self):
        # Find the position of the blank tile (0)
        for i in range(len(self.board)):
            for j in range(len(self.board[i])):
                if self.board[i][j] == 0:
                    return (i, j)
        return None
    
    def isGoal(self, goal):
        # Check if the current board matches the goal state
        return self.board == goal
    
    def generateChildNodes(self):
        # Generate child nodes by moving the blank tile in all possible directions
        board = self.board
        children = []
        x, y = self.getBlankPosition()
        moves = [(-1, 0), (1, 0), (0, -1), (0, 1)]
        # Up, Down, Left, Right
        for move in moves:
            new_x, new_y = x + move[0], y + move[1]
            if 0 <= new_x < len(self.board) and 0 <= new_y < len(self.board[0]):
                new = copy.deepcopy(board)
                new[x][y] = new[new_x][new_y]
                new[new_x][new_y] = 0
                childNode = Node(new, self, move)
                childNode.g = self.g + 1
                children.append(childNode)
        return children
    
    def getFullPath(self):
        path = []
        current = self
        while current:
            path.append(current)
            current = current.parent
        return path[::-1]
    
    def __lt__(self, other):
        # First priority: lower f
        if self.f != other.f:
            return self.f < other.f
        # Tie-breaker: lower h (closer to goal)
        return self.h < other.h

    
    def __eq__(self, other):
        """
        Check if two nodes have the same board state.
        """
        return self.board == other.board
    def __hash__(self):
        """
        Hash method for storing nodes in a set (closed list).
        """
        return hash(tuple(tuple(row) for row in self.board))