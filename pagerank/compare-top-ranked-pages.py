#!/usr/bin/python
# -*- coding: utf-8 -*-

import fileinput
import sys

N = 50
i = 0

a = []
b = []
c = []
d = []

# Fill the arrays with the elements from the file
for line in fileinput.input():
    if i < N:
        a.append(line.split()[0])
    else:
        if i < 2*N:
            b.append(line.split()[0])
        elif i < 3*N:
            c.append(line.split()[0])
        else:
            d.append(line.split()[0])
    i += 1

# print len(a), a
# print len(b), b
# print len(c), c
# print len(d), d

# Compare the topLists
def compareTopLists(a, b, c=[], d=[]):
    similarity = [0, 0, 0]
    for e in a:
        if e in b:
            similarity[0] += 1
        if e in c:
            similarity[1] += 1
        if e in d:
            similarity[2] += 1

    # return similarity
    return [["method1",similarity[0]],
            ["method2",similarity[1]],["method3",similarity[2]]]


# print a, b
print compareTopLists(a, b, c, d)
