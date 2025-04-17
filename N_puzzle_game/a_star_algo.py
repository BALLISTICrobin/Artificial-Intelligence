from node import Node
import math
import heapq
 
Hamming_Distance =1
Manhattan_Distance=2
Euclidean_Distance =3
Linear_Conflict =4

class AStarAlgo:
    def __init__(self,heuristic_fn):
        self.heuristic_fn = heuristic_fn
        self.expanded_nodes = 0
        self.explored_nodes = 0
        self.open_list = []
        self.closed_list = set()
        
    # def heuristic_func(self):
    #     if self.heuristic_fn == Hamming_Distance:
    #         return self.hamming_distance()
    #     elif self.heuristic_fn == Manhattan_Distance:
    #         return self.manhattan_distance()
    #     elif self.heuristic_fn == Euclidean_Distance:
    #         return self.euclidean_distance()
    #     elif self.heuristic_fn == Linear_Conflict:
    #         return self.linear_conflict()
    #     else:
    #         raise ValueError("Invalid heuristic function")
        
        
    def hamming_distance(self, node):
        distance = 0
        expected = 1
        for i in range(len(node.board)):
            for j in range(len(node.board[i])):
                if (i==len(node.board)-1 and j==len(node.board[i])-1) and node.board[i][j] != 0:
                    distance += 1
                else:
                    if node.board[i][j] != expected and node.board[i][j] != 0:
                        distance += 1
                    expected += 1
                    
        return distance
    
    def manhattan_distance(self, node):
        distance = 0
        for i in range(len(node.board)):
            for j in range(len(node.board[i])):
                if node.board[i][j] != 0:
                    x = (node.board[i][j]-1) // len(node.board)
                    y = (node.board[i][j]-1) % len(node.board)
                    distance += abs(x - i) + abs(y - j)
        return distance
    
    
    def euclidean_distance(self, node):
        distance = 0
        for i in range(len(node.board)):
            for j in range(len(node.board[i])):
                if node.board[i][j] != 0:
                    x = (node.board[i][j]-1) // len(node.board)
                    y = (node.board[i][j]-1) % len(node.board)
                    distance += math.sqrt((x - i) ** 2 + (y - j) ** 2)
        return distance
    
    
    
    def linear_conflict(self, node):
        n = len(node.board)
        manhattanDistance = 0
        conflicts = 0
    
        manhattanDistance = self.manhattan_distance(node)
    
        for i in range(n):
             for j in range(n):
                val1 = node.board[i][j]
                if val1 != 0 and (val1 - 1) // n == i:  
                    for k in range(j + 1, n):
                        val2 = node.board[i][k]
                        if val2 != 0 and (val2 - 1) // n == i:  
                            if (val1 - 1) % n > (val2 - 1) % n:
                                conflicts += 1
    
        for j in range(n):
            for i in range(n):
                val1 = node.board[i][j]
                if val1 != 0 and (val1 - 1) % n == j:
                    for k in range(i + 1, n):
                        val2 = node.board[k][j]
                        if val2 != 0 and (val2 - 1) % n == j: 
                            if (val1 - 1) // n > (val2 - 1) // n:
                                conflicts += 1
    
        return manhattanDistance + 2 * conflicts
        
    def solve(self, initial_board, goal_board):
       
        start_node = Node(initial_board)
        start_node.h = self.manhattan_distance(start_node)  
        start_node.f = start_node.g + start_node.h

       
        heapq.heappush(self.open_list, start_node)
        self.explored_nodes += 1

        while self.open_list:
      
            current_node = heapq.heappop(self.open_list)
            self.expanded_nodes += 1

           
            if current_node.isGoal(goal_board):
                return current_node.getFullPath()
        
            
            self.closed_list.add(current_node)

           
            for child in current_node.generateChildNodes():
                
                if child in self.closed_list:
                    continue
            
                
                if self.heuristic_fn == Hamming_Distance:
                    child.h = self.hamming_distance(child)
                elif self.heuristic_fn == Manhattan_Distance:
                    child.h = self.manhattan_distance(child)
                elif self.heuristic_fn == Euclidean_Distance:
                    child.h = self.euclidean_distance(child)
                elif self.heuristic_fn == Linear_Conflict:
                    child.h = self.linear_conflict(child)
            
                child.f = child.g + child.h
            
             
                in_open = False
                for open_node in self.open_list:
                    if open_node.board == child.board:
                        in_open = True
                        if child.f < open_node.f:
                           
                            open_node.g = child.g
                            open_node.h = child.h
                            open_node.f = child.f
                            open_node.parent = child.parent
                        break
            
                if not in_open:
                    heapq.heappush(self.open_list, child)
                    self.explored_nodes += 1
    
      
        return None
