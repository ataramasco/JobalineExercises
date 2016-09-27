package com.jobaline.uiautomation.framework.exception;

/**
 * Created by damian on 1/2/15.
 *
 * This Exception should be thrown by a method that was implemented but yet not tested or never used so we are still not confident on it.
 *
 * It would be a next step of java.lang.UnsupportedOperationException.
 *
 * For example, may be used in the next workflow:
 *
 * 	1 - Design a method
 * 	2 - Define the method signature and throw a java.lang.UnsupportedOperationException in the body
 * 	3 - Implement the method. Call MethodImplementedNotYetTestedException.throwException() in the first line
 * 	4 - Remove the line throwing the error, test the method (and fix if necessary) and start using it
 *
 * See the following stages of an example method that was implemented following the previous workflow:
 *
 * 	2-
 * 		public Integer getTotalCandidates(){
 *     		throw new UnsupportedOperationException();
 *        }
 *
 * 	3-
 * 		public Integer getTotalCandidates(){
 *     		MethodImplementedButNotYetTestedException.throwException();
 *     		return new CandidatesDAO().getTotalCandidates();
 *        }
 *
 * 	4-
 * 		public Integer getTotal(){
 *     		return new CandidatesDAO().getTotalCandidates();
 *        }
 *
 */
public class MethodImplementedButNotYetTestedException extends RuntimeException
{

	private MethodImplementedButNotYetTestedException()
	{
	}


	private MethodImplementedButNotYetTestedException(String message)
	{
		super(message);
	}


	public static void throwException()
	{
		throw new MethodImplementedButNotYetTestedException("This method was not tested/used yet, please make sure to test it if you plan to use it.");
	}
}
