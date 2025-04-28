import copy 

class Node:
    def __init__(self, board, parent=None, action=None):
        self.board = board  
        self.parent = parent 
        self.action = None 
        self.g = 0  
        self.h = 0 
        self.f = 0
        
    def getBlankPosition(self):
        
        for i in range(len(self.board)):
            for j in range(len(self.board[i])):
                if self.board[i][j] == 0:
                    return (i, j)
        return None
    
    def isGoal(self, goal):
      
        return self.board == goal
    
    def generateChildNodes(self):
     
        board = self.board
        children = []
        x, y = self.getBlankPosition()
        moves = [(-1, 0), (1, 0), (0, -1), (0, 1)]
       
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
        
        if self.f != other.f:
            return self.f < other.f
        
        return self.h < other.h

    
    def __eq__(self, other):
        
        return self.board == other.board
    def __hash__(self):
       
        return hash(tuple(tuple(row) for row in self.board))