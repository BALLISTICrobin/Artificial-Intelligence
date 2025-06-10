import os
import random
import csv
import uuid
from collections import defaultdict
import time
import concurrent.futures


def read_graph(file_path):
    graph = defaultdict(list)
    with open(file_path, 'r') as f:
        
        num_vertices, num_edges = map(int, f.readline().split())
        
        for _ in range(num_edges):
            u, v, w = map(int, f.readline().split())
            
            u, v = u - 1, v - 1
            
            graph[u].append((v, w))
            graph[v].append((u, w))
    return graph, num_vertices, num_edges


def compute_cut_weight(graph, X, Y):
    cut_weight = 0
    for u in X:
        for v, w in graph[u]:
            if v in Y:
                cut_weight += w
    return cut_weight


def randomized_maxcut(graph, n, runs=100):
    total_cut_weight = 0
    for _ in range(runs):
        X, Y = set(), set()
        for v in range(n):
            if random.random() >= 0.5:
                X.add(v)
            else:
                Y.add(v)
        cut_weight = compute_cut_weight(graph, X, Y)
        total_cut_weight += cut_weight
    return total_cut_weight / runs


def greedy_maxcut(graph, number_of_vertices):
    
    highest_edge_weight = -1
    selected_edge = None
    

    for current_vertex in graph:
        for neighbor_vertex, edge_weight in graph[current_vertex]:
            if edge_weight > highest_edge_weight:
                highest_edge_weight = edge_weight
                selected_edge = (current_vertex, neighbor_vertex)
    
   
    partition_X = set()
    partition_Y = set()
    
    
    first_endpoint = selected_edge[0]
    second_endpoint = selected_edge[1]
    partition_X.add(first_endpoint)
    partition_Y.add(second_endpoint)
    
   
    unassigned_vertices = set()
    for vertex in range(number_of_vertices):
        if vertex != first_endpoint and vertex != second_endpoint:
            unassigned_vertices.add(vertex)
    
    
    for current_vertex in unassigned_vertices:
       
        contribution_to_X = 0
        for neighbor_vertex, edge_weight in graph[current_vertex]:
            if neighbor_vertex in partition_Y:
                contribution_to_X += edge_weight
        
        contribution_to_Y = 0
        for neighbor_vertex, edge_weight in graph[current_vertex]:
            if neighbor_vertex in partition_X:
                contribution_to_Y += edge_weight
        
        
        if contribution_to_X > contribution_to_Y:
            partition_X.add(current_vertex)
        else:
            partition_Y.add(current_vertex)
    
    
    total_cut_weight = compute_cut_weight(graph, partition_X, partition_Y)
    
   
    return partition_X, partition_Y, total_cut_weight

def semi_greedy_maxcut(graph, n, alpha=0.5):
   
    X = set()
    Y = set()
    
    
    max_weight = -1
    max_edge = None
    for vertex in graph:
        for neighbor, weight in graph[vertex]:
            if weight > max_weight:
                max_weight = weight
                max_edge = (vertex, neighbor)
    
    
    X.add(max_edge[0])
    Y.add(max_edge[1])
    
    
    unassigned_vertices = set(range(n)) - {max_edge[0], max_edge[1]}
    
   
    while unassigned_vertices:
       
        vertex_info = []
        for vertex in unassigned_vertices:
            
            contribution_to_X = 0
            contribution_to_Y = 0
            
            
            for neighbor, weight in graph[vertex]:
                if neighbor in Y:
                    contribution_to_X += weight
            
            
            for neighbor, weight in graph[vertex]:
                if neighbor in X:
                    contribution_to_Y += weight
            
            
            greedy_value = max(contribution_to_X, contribution_to_Y)
            
           
            vertex_info.append((vertex, contribution_to_X, contribution_to_Y, greedy_value))
        
       
        all_contributions = []
        for vertex, sigma_X, sigma_Y, greedy_value in vertex_info:
            all_contributions.append(sigma_X)
            all_contributions.append(sigma_Y)
        
    
        if all_contributions: 
            min_contribution = min(all_contributions)
            max_contribution = max(all_contributions)
        else:
            min_contribution = 0
            max_contribution = 0
        
        
        mu = min_contribution + alpha * (max_contribution - min_contribution)
        
       
        restricted_candidate_list = []
        for vertex, sigma_X, sigma_Y, greedy_value in vertex_info:
            if greedy_value >= mu:
                restricted_candidate_list.append((vertex, sigma_X, sigma_Y))
        
        
        if not restricted_candidate_list:
            for vertex, sigma_X, sigma_Y, max_contribution in vertex_info:
                restricted_candidate_list.append((vertex, sigma_X, sigma_Y))
        
     
        selected_vertex, sigma_X, sigma_Y = random.choice(restricted_candidate_list)
        
        
        if sigma_X > sigma_Y:
            X.add(selected_vertex)
        else:
            Y.add(selected_vertex)
        
        
        unassigned_vertices.remove(selected_vertex)
    
    
    Total_cut_weight = compute_cut_weight(graph, X, Y)
    
    return X, Y, Total_cut_weight

def local_search(graph, number_of_vertices, initial_partition_X, initial_partition_Y):
   
    iteration_count = 0
    current_partition_X = initial_partition_X.copy() 
    current_partition_Y = initial_partition_Y.copy()  
    
    
    while True:

        best_improvement = -float('infinity')  
        best_vertex_to_move = None  
        
       
        for vertex in range(number_of_vertices):
            
            contribution_to_X = 0
            for neighbor, weight in graph[vertex]:
                if neighbor in current_partition_X:
                    contribution_to_X += weight
            
            
            contribution_to_Y = 0
            for neighbor, weight in graph[vertex]:
                if neighbor in current_partition_Y:
                    contribution_to_Y += weight
            
            
            change_in_cut_weight = 0
            if vertex in current_partition_X:
                
                change_in_cut_weight = contribution_to_X - contribution_to_Y
            else:
                
                change_in_cut_weight = contribution_to_Y - contribution_to_X
            
          
            if change_in_cut_weight > best_improvement:
                best_improvement = change_in_cut_weight
                best_vertex_to_move = vertex
        
 
        if best_improvement > 0:
           
            if best_vertex_to_move in current_partition_X:
                current_partition_X.remove(best_vertex_to_move)
                current_partition_Y.add(best_vertex_to_move)
            else:
                current_partition_Y.remove(best_vertex_to_move)
                current_partition_X.add(best_vertex_to_move)
            
           
            iteration_count += 1
        else:
            
            break
    
    
    final_cut_weight = compute_cut_weight(graph, current_partition_X, current_partition_Y)
    
  
    return current_partition_X, current_partition_Y, final_cut_weight, iteration_count


def grasp_maxcut(graph, n, max_iterations=100, alpha=0.5):
    best_X, best_Y = None, None
    best_cut_weight = -float('inf')
    total_iterations = 0
    
    for iter in range(max_iterations):
       
        X, Y, total_cut_weight = semi_greedy_maxcut(graph, n, alpha)
        
        X, Y, cut_weight, iter_count = local_search(graph, n, X, Y)
        total_iterations += iter_count
        if cut_weight > best_cut_weight:
            best_X, best_Y = X, Y
            best_cut_weight = cut_weight
    
    return best_X, best_Y, best_cut_weight, max_iterations


def process_graph(args):
    filename, input_dir, alpha, known_bests = args
    file_path = os.path.join(input_dir, filename)
    graph, num_vertices, num_edges = read_graph(file_path)

    num_random_runs = 50  # reduced for speed
    rand_cut = randomized_maxcut(graph, num_vertices, runs=num_random_runs)

    X, Y, greedy_cut = greedy_maxcut(graph, num_vertices)
    X, Y, semi_greedy_cut = semi_greedy_maxcut(graph, num_vertices, alpha)
    X, Y, ls_cut, ls_iterations = local_search(graph, num_vertices, X, Y)
    X, Y, grasp_cut, grasp_iterations = grasp_maxcut(graph, num_vertices, max_iterations=50, alpha=alpha)
    grasp_avg_value = grasp_cut

    graph_id = filename.split('.')[0]
    known_best = known_bests.get(graph_id, "N/A")

    return [
        graph_id,
        num_vertices,
        num_edges,
        f"{rand_cut:.0f}",
        f"{greedy_cut:.0f}",
        f"{semi_greedy_cut:.0f}",
        ls_iterations,
        f"{ls_cut:.0f}",
        grasp_iterations,
        f"{grasp_avg_value:.0f}",
        known_best
    ]

def main():
    input_dir = "../set1"
    output_csv = "2105019.csv"
    alpha = 0.5

    known_bests = {
        "g1": 12078, "g2": 12084, "g3": 12077, "g11": 627, "g12": 621, "g13": 645,
        "g14": 3187, "g15": 3169, "g16": 3172, "g22": 14123, "g23": 14129,
        "g24": 14131, "g32": 1560, "g33": 1537, "g34": 1541, "g35": 8000,
        "g36": 7996, "g37": 8009, "g43": 7027, "g44": 7022, "g45": 7020,
        "g48": 6000, "g49": 6000, "g50": 5988
    }

    # Gather files to process
    graph_files = sorted(
        [f for f in os.listdir(input_dir) if f.endswith(".rud")],
        key=lambda x: int(x[1:-4])
    )

    with open(output_csv, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow([
            "Problem", "", "", "Constructive algorithm", "", "", "Local search", "", "GRASP", "", "Known best solution or upper bound"
        ])
        writer.writerow([
            "Name", "|V| or n", "|E| or m",
            "Simple Randomized or Randomized-1",
            "Simple Greedy or Greedy-1",
            f"Semi-greedy-1 (alpha={alpha})",
            "No. of iterations", "Average value",
            "No. of iterations", "Best value",
            " "
        ])

       
        task_args = [(filename, input_dir, alpha, known_bests) for filename in graph_files]

        
        with concurrent.futures.ProcessPoolExecutor() as executor:
            for result_row in executor.map(process_graph, task_args):
                writer.writerow(result_row)
                print(f"Processed {result_row[0]}")

if __name__ == "__main__":
    main()