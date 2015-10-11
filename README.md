# Description

A MapReduce framework in Java, implementing the following generic behavior:

Four input parameters are provided through the command line:

* an `input directory` where input files can be found
* an `output directory` where output files will be produced
* the number of `mapper` threads to be used
* the number of `reducer` threads to be used

The files in the input directory are parsed and equally distributed to mapper
threads. Each mapper, applies the map function to the contents of each file
and stores its results to a shared structure, which indexes each key (String)
to a list of values (also Strings).

The results of the map function are appropriately partitioned and fed into the
reducer threads. Each reducer, applies the reduce function to the elements of
its corresponding partition and stores the results to a shared structure, which
indexes each key (String) to the final value (also String).

Finally, an output file is created containing the final key-value pairs.

<hr/>

The framework could be used by implementing the following functions:

* `void map(final File fileEntry, Map<String, ArrayList<String>> map) {}`
* `void reduce(String key, ArrayList<String>value) {}`

Map function is applied to the files in the input directory.
Map structure is used in order to store the results of map function and is
initially empty.

Reduce function is applied to the key-value pairs which were produced by the
map function.

<hr/>

# Examples

## WordCounter

A standard word count example.

##### Compiling and Running Instructions
```
javac src/WordCounter.java
java src/WordCounter /TestFiles/WordCounter/ /TestFiles/out.txt numOfMappers numOfReducers
```

##### Test Case

* File<sub>1</sub>: `"file one file"`
* File<sub>2</sub>: `"file two file"`
* File<sub>3</sub>: `"file three file"`
* File<sub>4</sub>: `"file four file"`
* File<sub>5</sub>: `"file five file"`
* File<sub>6</sub>: `"file six file"`
* File<sub>7</sub>: `"file seven file"`
* File<sub>8</sub>: `"file eight file"`

##### Output
```
eight 1
file 16
five 1
four 1
one 1
seven 1
six 1
three 1
two 1
```

## GraphConverter

The MapReduce framework could be used in order to convert a graph in edge list
format (distributed across a number of separate files) into adjacency list
format.

##### Compiling and Running Instructions
```
javac src/GraphConverter.java
java src/GraphConverter /TestFiles/GraphConverter/ /TestFiles/out.txt numOfMappers numOfReducers
```

##### Test Case

* File<sub>1</sub>: `(0,1) (2,0) (3,0)`
* File<sub>2</sub>: `(3,1) (4,0)`
* File<sub>3</sub>: `(4,3) (5,0)`
* File<sub>4</sub>: `(5,4) (6,0)`
* File<sub>5</sub>: `(6,3) (7,3)`
* File<sub>6</sub>: `(7,5) (8,3)`
* File<sub>7</sub>: `(8,4) (1,8)`
* File<sub>8</sub>: `(9,0) (2,5)`

##### Output
```
0 # 1 2 3 4 5 6 9
1 # 0 3 8
2 # 0 5
3 # 0 1 4 6 7 8
4 # 0 3 5 8
5 # 0 2 4 7
6 # 0 3
7 # 3 5
8 # 1 3 4
9 # 0
```

## CommonFriendsDetector

A social network is an undirected graph where nodes represent people and
edges friendships. Two people `a` and `b` in a social network have a common
friend `c` whenever edges between `(a, c)` and `(b, c)` exist. Note that this
does not require that `a` and `b` are friends. The MapReduce framework could
be used in order to detect common friends in a social network, by providing an
output file where entries would follow the following format:

`friend1 friend2 # commonfriend1 commonfriend2...`

##### Compiling and Running Instructions
```
javac src/CommonFriendsDetector.java
java src/CommonFriendsDetector /TestFiles/CommonFriendsDetector/ /TestFiles/out.txt numOfMappers numOfReducers
```

##### Test Case

* File<sub>1</sub>: `(0,1) (2,0) (3,0)`
* File<sub>2</sub>: `(3,1) (4,0)`
* File<sub>3</sub>: `(4,3) (5,0)`
* File<sub>4</sub>: `(5,4) (6,0)`
* File<sub>5</sub>: `(6,3) (7,3)`
* File<sub>6</sub>: `(7,5) (8,3)`
* File<sub>7</sub>: `(8,4) (1,8)`
* File<sub>8</sub>: `(9,0) (2,5)`

##### Output
```
0 1 # 3           |       1 7 # 3           |       3 9 # 0
0 2 # 5           |       1 8 # 3           |       4 5 # 0
0 3 # 1 4 6       |       1 9 # 0           |       4 6 # 0 3
0 4 # 3 5         |       2 3 # 0           |       4 7 # 3 5
0 5 # 2 4         |       2 4 # 0 5         |       4 8 # 3
0 6 # 3           |       2 5 # 0           |       4 9 # 0
0 7 # 3 5         |       2 6 # 0           |       5 6 # 0
0 8 # 1 3 4       |       2 7 # 5           |       5 8 # 4
1 2 # 0           |       2 9 # 0           |       5 9 # 0
1 3 # 0 8         |       3 4 # 0 8         |       6 7 # 3
1 4 # 0 3 8       |       3 5 # 0 4 7       |       6 8 # 3
1 5 # 0           |       3 6 # 0           |       6 9 # 0
1 6 # 0 3         |       3 8 # 1 4         |       7 8 # 3
```

## TriangleCounter

In undirected graphs, a triangle is a collection of 3 nodes that each one is
connected with the other two. For example, `a`, `b`, and `c` form a triangle if
`(a, b)`, `(c, a)`, and `(c, b)` are edges in the graph. The MapReduce
framework could be used in order to calculate the number of triangles that each
person in the social network appears in, by providing an output file where
entries would follow the following format:

`person number_of_triangles_person_is_in`

##### Compiling and Running Instructions
```
javac src/TriangleCounter.java
java src/TriangleCounter /TestFiles/TriangleCounter/ /TestFiles/out.txt numOfMappers numOfReducers
```

##### Test Case

* File<sub>1</sub>: `(0,1) (2,0) (3,0)`
* File<sub>2</sub>: `(3,1) (4,0)`
* File<sub>3</sub>: `(4,3) (5,0)`
* File<sub>4</sub>: `(5,4) (6,0)`
* File<sub>5</sub>: `(6,3) (7,3)`
* File<sub>6</sub>: `(7,5) (8,3)`
* File<sub>7</sub>: `(8,4) (1,8)`
* File<sub>8</sub>: `(9,0) (2,5)`

##### Output
```
0 5
1 2
2 1
3 5
4 3
5 3
6 1
7 0
8 2
9 0
```
