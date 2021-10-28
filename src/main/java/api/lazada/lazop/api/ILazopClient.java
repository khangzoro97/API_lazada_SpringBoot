package api.lazada.lazop.api;

import api.lazada.lazop.util.ApiException;

/**
 * Lazada Open Platform API client interface.
 * 
 * @author carver.gu
 * @since Feb 4, 2018
 */
public interface ILazopClient {

	/**
	 * Execute API request without access token.
	 */
	public LazopResponse execute(LazopRequest request) throws ApiException;

	/**
	 * Execute API request with access token.
	 */
	public LazopResponse execute(LazopRequest request, String accessToken) throws ApiException;

}
