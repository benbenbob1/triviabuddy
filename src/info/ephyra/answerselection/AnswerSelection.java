package info.ephyra.answerselection;

import info.ephyra.answerselection.filters.Filter;
import info.ephyra.io.MsgPrinter;
import info.ephyra.search.Result;

import java.util.*;

import me.xdrop.fuzzywuzzy.FuzzySearch;

/**
 * <p>The <code>AnswerSelection</code> component applies <code>Filters</code> to
 * <code>Results</code> to promote promising results, to drop results that are
 * unlikely to answer the question and to derive additional results from the raw
 * results returned by the <code>Searchers</code>.</p>
 * 
 * @author Nico Schlaefer
 * @version 2006-06-28
 */
public class AnswerSelection {

    public static <K, V extends Comparable< ? super V>> Map<K, V> sortMapByValues(final Map <K, V> mapToSort, final boolean inverted)
    {
        List<Map.Entry<K, V>> entries =
                new ArrayList<Map.Entry<K, V>>(mapToSort.size());

        entries.addAll(mapToSort.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare(
                    final Map.Entry<K, V> entry1,
                    final Map.Entry<K, V> entry2)
            {
                int compare = entry1.getValue().compareTo(entry2.getValue());
                if (!inverted) {
                    compare = 0 - compare;
                }
                return compare;
            }
        });

        Map<K, V> sortedMap = new LinkedHashMap<K, V>();

        for (Map.Entry<K, V> entry : entries)
        {
            sortedMap.put(entry.getKey(), entry.getValue());

        }

        return sortedMap;

    }

	/**
	 * The <code>Filters</code> that are applied to the <code>Results</code>.
	 * Filters are applied in the order in which they appear in this list.
	 */
	private static ArrayList<Filter> filters = new ArrayList<Filter>();
	
	/**
	 * Registers a <code>Filter</code>. Filters are applied in the order in
	 * which they are registered.
	 * 
	 * @param filter <code>Filter</code> to add
	 */
	public static void addFilter(Filter filter) {
		filters.add(filter);
	}
	
	/**
	 * Unregisters all <code>Filters</code>.
	 */
	public static void clearFilters() {
		filters.clear();
	}
	
	/**
	 * Applies <code>Filters</code> to the <code>Results</code> from the search
	 * component and returns up to <code>maxResults</code> results with a score
	 * of at least <code>minScore</code>.
	 * 
	 * @param results search results
	 * @param maxResults maximum number of results to be returned
	 * @param minScore minimum score of a result that is returned
	 * @return up to <code>maxResults</code> results
	 */
	public static Result[] getResults(Result[] results, int maxResults,
									  float minScore) {
		// apply filters
		for (Filter filter : filters) {
			MsgPrinter.printFilterStarted(filter, results.length);
			results = filter.apply(results);
			MsgPrinter.printFilterFinished(filter, results.length);
		}
		
		// get up to maxResults results with a score of at least minScore
		ArrayList<Result> resultsList = new ArrayList<Result>();
		for (Result result : results) {
			if (maxResults == 0) break;
			
			if (result.getScore() >= minScore) {
				resultsList.add(result);
				maxResults--;
			}
		}
		
		return resultsList.toArray(new Result[resultsList.size()]);
	}

	/**
	 * Applies <code>Filters</code> to the <code>Results</code> from the search
	 * component and returns up to <code>maxResults</code> results with a score
	 * of at least <code>minScore</code>, matching against provided answers
	 *
	 * @param results search results
	 * @param answers possible answers to match against
	 * @param maxResults maximum number of results to be returned
	 * @param minScore minimum score of a result that is returned
	 * @return up to <code>maxResults</code> results
	 */
	public static Result[] getResultsWithAnswerMatching(Result[] results, String[] answers, int maxResults,
		float minScore, boolean isInverse) {

		int[] answerCounts = new int[answers.length];
		int resultCount = results.length;

		boolean foundAnyAnswer = false;

        Map<String, Integer> rankedAnswers = new HashMap<String, Integer>();

        for (String answer : answers) {
            rankedAnswers.put(answer, 0);
        }

		for (Result result : results) {
			for (String answer : answers) {
				//Fuzzy search for answer in result
				/*if (result.getAnswer().contains(answer)) {
					answerCounts[a] += 1;
				}*/
				int ratio = FuzzySearch.tokenSetRatio(answer, result.getAnswer());
				//System.out.println("Ratio of "+answer+" in "+result.getAnswer()+" is >>>>>> "+ratio);
				if (ratio > 50) {
				    int curVal = rankedAnswers.get(answer);
					rankedAnswers.put(answer, curVal+1);
					foundAnyAnswer = true;
				}
			}
		}

		// apply filters
		for (Filter filter : filters) {
			MsgPrinter.printFilterStarted(filter, results.length);
			results = filter.apply(results);
			MsgPrinter.printFilterFinished(filter, results.length);
		}

		// get up to maxResults results with a score of at least minScore
		ArrayList<Result> resultsList = new ArrayList<Result>();
		for (Result result : results) {
			if (maxResults == 0) break;

			if (result.getScore() >= minScore) {
				resultsList.add(result);
				maxResults--;
			}
		}

		/*int maxCount = 0;
		int minCount = 100;
		int bestAnswer = 0, worstAnswer = 0;
		for (int a = 0; a<answers.length; a++) {
			int count = answerCounts[a];
			if (count > maxCount) {
				maxCount = count;
				bestAnswer = a;
			}

			if (count < minCount) {
				minCount = count;
				worstAnswer = a;
			}
		}*/

		if (resultsList.size() > 0) {
		    for (Result result : resultsList) {
                for (String answer : answers) {
                    int ratioToActualAnswer = FuzzySearch.tokenSetRatio(answer, result.getAnswer());
                    if (ratioToActualAnswer >= 75) {
                        foundAnyAnswer = true;
                        System.out.println("Found actual answer!!");
                        rankedAnswers.put(answer, rankedAnswers.get(answer)+(resultCount/2));
                    }
                }
            }
        }

        //ValueComparator baseValueComparator = new ValueComparator(rankedAnswers, isInverse);
        //Tree = new TreeMap<String, Integer>(baseValueComparator);
        //Map<String, Integer> sortedAnswers = AnswerSelection.sortMapByValues(rankedAnswers, isInverse);
        resultsList.clear();

        System.out.println("Inverted? "+isInverse);

        //System.out.println("Unsorted: "+rankedAnswers.toString());
        //System.out.println("Sorted: "+sortedAnswers.toString());

        if (!foundAnyAnswer) {
			System.out.println("Answer selection: No hack answer found :(");
		} else {
            for (Map.Entry entry : rankedAnswers.entrySet()) {
                double percent = ((Integer)entry.getValue())/((double)resultCount);
                if (isInverse) {
                    percent = 1 - percent;
                }
                //String answer = entry.getKey() + " (" + String.format("%2.2f", percent*100) + "%)";
                Result r = new Result((String)entry.getKey());
                r.setScore((float)(percent*100));
                resultsList.add(r);
            }
			/*if (isInverse) {
				System.out.println("Answer was inverted");
				int temp = bestAnswer;
				bestAnswer = worstAnswer;
				worstAnswer = temp;
			}

			System.out.println("Answer selection: Hack answer found! "+answers[bestAnswer]+" ("+maxCount+"/"+resultCount+")");
			System.out.println("Answer selection: Worst answer: "+answers[worstAnswer]+" ("+minCount+"/"+resultCount+")");*/


		}

		return resultsList.toArray(new Result[resultsList.size()]);
	}
}
