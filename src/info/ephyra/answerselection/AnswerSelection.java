package info.ephyra.answerselection;

import info.ephyra.answerselection.filters.Filter;
import info.ephyra.io.MsgPrinter;
import info.ephyra.search.Result;

import java.util.ArrayList;

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
		float minScore) {
		// apply filters
		/*for (Filter filter : filters) {
			MsgPrinter.printFilterStarted(filter, results.length);
			results = filter.apply(results);
			MsgPrinter.printFilterFinished(filter, results.length);
		}*/


		int[] answerCounts = new int[answers.length];

		// get up to maxResults results with a score of at least minScore
		ArrayList<Result> resultsList = new ArrayList<Result>();
		for (Result result : results) {

			//System.out.println("A result: "+result.getAnswer());

			for (int a = 0; a<answers.length; a++) {
				String answer = answers[a];
				if (result.getAnswer().contains(answer)) {
					answerCounts[a] += 1;
				}
			}


			/*if (maxResults == 0) break;

			if (result.getScore() >= minScore) {
				resultsList.add(result);
				maxResults--;
			}*/
		}

		int maxCount = -1;
		int bestAnswer = 0;
		for (int a = 0; a<answers.length; a++) {
			int count = answerCounts[a];
			if (count > maxCount) {
				maxCount = count;
				bestAnswer = a;
			}
		}

		if (maxCount == -1) {
			System.out.println("No hack answer found :(");
		} else {
			System.out.println("Hack answer found! "+answers[bestAnswer]);
		}

		return resultsList.toArray(new Result[resultsList.size()]);
	}
}
