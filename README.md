# Palindrome Finder (Eertree)

A Java implementation that scans an input string and returns all **disjoint** palindromes of length **≥ 3**. The core detection uses a **Palindromic Tree (a.k.a. Eertree)** for near-linear processing, followed by a greedy selection strategy to ensure non-overlapping results while preferring longer palindromes when candidates collide.

---

## Quick Start

* **JDK:** 17+
* **Build tools:** Maven (tests use JUnit 5 + AssertJ)

**File layout (suggested):**

```
src/
  main/java/com/example/palindrome/PalindromeFinderEertree.java
  test/java/com/example/palindrome/PalindromeFinderEertreeTest.java
```

---

## Public API

```java
List<String> findPalindromes(String sentence)
```

**Behavior**

* Returns an **unmodifiable** list of all **non-overlapping** palindromes (length ≥ 3), ordered **left→right**.
* **Validation:**

    * `null` → `NullPointerException("sentence must not be null")`.
    * Any character that is not `Character.isLetterOrDigit` → `IllegalArgumentException` with the **exact index** of the first offending character.
* **Case-sensitive** and **Unicode-aware** (uses `Character.isLetterOrDigit`). If your inputs may contain decomposed characters (NFD), normalize to NFC first.

---

## How Requirements Are Met

**Original requirements** and their mapping to the implementation/tests:

1. **Function signature**: `List<String> findPalindromes(String sentence)`

    * Implemented as the public method of `PalindromeFinderEertree`.

2. **Accept only letters and digits**

    * Method `validateContainsOnlyLettersOrDigits` iterates characters and rejects the first non-alphanumeric with an informative message.
    * Tests: parameterized suite covers spaces, underscores, dashes, punctuation, tabs, and newlines; also checks the **exact index** in the message.

3. **No nested palindromes; all palindromes are disjoint**

    * Even if input accidentally contains overlaps, a greedy selection (sorted by length **desc**, then start index) picks a **maximal set of disjoint** palindromes, effectively honoring the “disjoint” requirement and preventing nested picks.
    * Tests: `ababa` → only `"ababa"`; repetitive `aaaaa` → only the single longest; adjacency handled correctly (`"abacdc"` → `"aba","cdc"`).

4. **Minimum length > 2**

    * Filter: candidates are accepted only when `length ≥ 3`.
    * Tests: inputs shorter than 3 return an empty list.

5. **Deliverables: one implementation file, one test file**

    * Provided as `PalindromeFinderEertree.java` and `PalindromeFinderEertreeTest.java`.

6. **Return all palindromes** (within the constraints)

    * All **disjoint** palindromes are returned; when overlaps exist, the longest wins, ensuring a deterministic, spec-aligned result set.
    * Output order is stable (by start index).

---

## Algorithm Overview

### 1) Detection with **Eertree** (Palindromic Tree)

The Eertree stores every distinct palindromic substring as a node. It supports appending characters one-by-one and updating/creating the node that represents the **longest palindromic suffix** of the processed prefix.

**Node structure**

* `palindromeLength` – length of the palindrome represented by the node.
* `suffixLinkNodeIndex` – link to the node representing the longest proper palindromic suffix.
* `transitionsByCharacter` – edges by characters to extended palindromes.

Two special roots are used:

* Length **-1** (imaginary) and length **0** (empty string). They simplify boundary cases.

**Per-character update (high level)**

1. From the current longest-suffix node, follow `suffixLink`s until you can mirror the new char to the left and match.
2. If an outgoing transition for the new char exists, reuse it (no new node).
3. Otherwise, create a new node with `length = matchedNode.length + 2`, patch its suffix link by following suffix links and transitions.
4. The resulting node is the **new longest palindromic suffix**.

We record the length of this suffix after each append; if it is ≥ 3, we add the corresponding interval as a **candidate** palindrome ending at the current index.

**Complexity**

* Building the Eertree: **O(n)** time, **O(n)** space.
* Number of distinct palindromic substrings ≤ n, so we collect at most O(n) candidates.

### 2) Greedy selection of **disjoint** palindromes

1. Sort candidates by length **descending**, then by start index.
2. Sweep through candidates and keep an interval only if it doesn’t overlap positions we’ve already taken.
3. Sort the chosen intervals by start index and slice substrings from the original sentence.

**Complexity**: Sorting candidates is **O(k log k)** where k ≤ n; the acceptance sweep is **O(k)**.

---

## Examples

| Input                | Output (list of palindromes)   |
| -------------------- | ------------------------------ |
| `"abcba12321xyzzyx"` | `["abcba", "12321", "xyzzyx"]` |
| `"ababa"`            | `["ababa"]`                    |
| `"abacdcxy"`         | `["aba", "cdc"]`               |
| `"x12321y"`          | `["12321"]`                    |
| `"abcdefg12345"`     | `[]`                           |

---

## Design Notes

* **Deterministic ordering**: results are always left-to-right.
* **Unmodifiable result**: returned list cannot be altered by the caller.
* **Unicode**: `Character.isLetterOrDigit` allows letters/digits from many scripts. If inputs may include combining marks (NFD), normalize to NFC to avoid rejections.
* **Case sensitivity**: no case folding performed by default.

---

## Testing Highlights (GWT style)

* **Input Validation**: rejects non-alphanumeric inputs with precise index; `null` → NPE; short inputs → empty list.
* **Finding Palindromes**: single and multiple cases, adjacency, mixed alphanumeric, Unicode with diacritics (after NFC normalization in tests).
* **Selection Strategy**: overlapping candidates → pick the longest; repetitive inputs collapse to one longest palindrome; final order by start index.
* **Performance/Scale (Smoke)**: large string with one embedded long palindrome is handled correctly.

---
