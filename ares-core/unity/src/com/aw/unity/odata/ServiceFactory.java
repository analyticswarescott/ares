
package com.aw.unity.odata;

import org.apache.olingo.odata2.api.ODataServiceFactory;

public abstract class ServiceFactory extends ODataServiceFactory {

//	private static ODataService _svc;
//	private HashMap<String, UnityEdmProvider> _providers = new HashMap<String, UnityEdmProvider>() ;
//
//  @Override
//  public ODataService createService(final ODataContext ctx) throws ODataException {
//
//
//
//    EdmProvider edmProvider = new UnityEdmProvider();
//
//    ODataSingleProcessor singleProcessor = new UnityODataSingleProcessor();
//
//
//   // if (_svc == null) {
//    	_svc = createODataSingleProcessorService(getProvider(), singleProcessor);
// //   }
//
//    return _svc;
//  }
//
//
//  private UnityEdmProvider getProvider() {
//	  //returns install for tenant
//	  String tid = UnityMgr.getTenantID();
//
//	  UnityEdmProvider p= null;
//
//	  if (!_providers.containsKey(tid)) {
//		  p = new UnityEdmProvider();
//		  _providers.put(tid, p);
//	  }
//
//	  return _providers.get(tid);
//
//  }
//

}
