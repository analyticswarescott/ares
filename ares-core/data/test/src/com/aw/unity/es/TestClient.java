package com.aw.unity.es;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.exists.ExistsRequest;
import org.elasticsearch.action.exists.ExistsRequestBuilder;
import org.elasticsearch.action.exists.ExistsResponse;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.explain.ExplainRequestBuilder;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.fieldstats.FieldStatsRequest;
import org.elasticsearch.action.fieldstats.FieldStatsRequestBuilder;
import org.elasticsearch.action.fieldstats.FieldStatsResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptRequest;
import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptRequestBuilder;
import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptResponse;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptRequest;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptRequestBuilder;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptResponse;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptRequest;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptRequestBuilder;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptResponse;
import org.elasticsearch.action.percolate.MultiPercolateRequest;
import org.elasticsearch.action.percolate.MultiPercolateRequestBuilder;
import org.elasticsearch.action.percolate.MultiPercolateResponse;
import org.elasticsearch.action.percolate.PercolateRequest;
import org.elasticsearch.action.percolate.PercolateRequestBuilder;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollRequestBuilder;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.suggest.SuggestRequest;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.action.termvectors.MultiTermVectorsRequest;
import org.elasticsearch.action.termvectors.MultiTermVectorsRequestBuilder;
import org.elasticsearch.action.termvectors.MultiTermVectorsResponse;
import org.elasticsearch.action.termvectors.TermVectorsRequest;
import org.elasticsearch.action.termvectors.TermVectorsRequestBuilder;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.support.Headers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;

@SuppressWarnings("all")
public class TestClient implements Client {

	@Override
	public void close() {


	}

	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> ActionFuture<Response> execute(
			Action<Request, Response, RequestBuilder> action, Request request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> void execute(
			Action<Request, Response, RequestBuilder> action, Request request, ActionListener<Response> listener) {


	}

	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> RequestBuilder prepareExecute(
			Action<Request, Response, RequestBuilder> action) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ThreadPool threadPool() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public AdminClient admin() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<IndexResponse> index(IndexRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void index(IndexRequest request, ActionListener<IndexResponse> listener) {


	}

	@Override
	public IndexRequestBuilder prepareIndex() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<UpdateResponse> update(UpdateRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void update(UpdateRequest request, ActionListener<UpdateResponse> listener) {


	}

	@Override
	public UpdateRequestBuilder prepareUpdate() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public UpdateRequestBuilder prepareUpdate(String index, String type, String id) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public IndexRequestBuilder prepareIndex(String index, String type) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public IndexRequestBuilder prepareIndex(String index, String type, String id) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<DeleteResponse> delete(DeleteRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void delete(DeleteRequest request, ActionListener<DeleteResponse> listener) {


	}

	@Override
	public DeleteRequestBuilder prepareDelete() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public DeleteRequestBuilder prepareDelete(String index, String type, String id) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<BulkResponse> bulk(BulkRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void bulk(BulkRequest request, ActionListener<BulkResponse> listener) {


	}

	@Override
	public BulkRequestBuilder prepareBulk() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<GetResponse> get(GetRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void get(GetRequest request, ActionListener<GetResponse> listener) {


	}

	@Override
	public GetRequestBuilder prepareGet() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public GetRequestBuilder prepareGet(String index, String type, String id) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public PutIndexedScriptRequestBuilder preparePutIndexedScript() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public PutIndexedScriptRequestBuilder preparePutIndexedScript(String scriptLang, String id, String source) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void deleteIndexedScript(DeleteIndexedScriptRequest request, ActionListener<DeleteIndexedScriptResponse> listener) {


	}

	@Override
	public ActionFuture<DeleteIndexedScriptResponse> deleteIndexedScript(DeleteIndexedScriptRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public DeleteIndexedScriptRequestBuilder prepareDeleteIndexedScript() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public DeleteIndexedScriptRequestBuilder prepareDeleteIndexedScript(String scriptLang, String id) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void putIndexedScript(PutIndexedScriptRequest request, ActionListener<PutIndexedScriptResponse> listener) {


	}

	@Override
	public ActionFuture<PutIndexedScriptResponse> putIndexedScript(PutIndexedScriptRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public GetIndexedScriptRequestBuilder prepareGetIndexedScript() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public GetIndexedScriptRequestBuilder prepareGetIndexedScript(String scriptLang, String id) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void getIndexedScript(GetIndexedScriptRequest request, ActionListener<GetIndexedScriptResponse> listener) {


	}

	@Override
	public ActionFuture<GetIndexedScriptResponse> getIndexedScript(GetIndexedScriptRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<MultiGetResponse> multiGet(MultiGetRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void multiGet(MultiGetRequest request, ActionListener<MultiGetResponse> listener) {


	}

	@Override
	public MultiGetRequestBuilder prepareMultiGet() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<CountResponse> count(CountRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void count(CountRequest request, ActionListener<CountResponse> listener) {


	}

	@Override
	public CountRequestBuilder prepareCount(String... indices) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<ExistsResponse> exists(ExistsRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void exists(ExistsRequest request, ActionListener<ExistsResponse> listener) {


	}

	@Override
	public ExistsRequestBuilder prepareExists(String... indices) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<SuggestResponse> suggest(SuggestRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void suggest(SuggestRequest request, ActionListener<SuggestResponse> listener) {


	}

	@Override
	public SuggestRequestBuilder prepareSuggest(String... indices) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<SearchResponse> search(SearchRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void search(SearchRequest request, ActionListener<SearchResponse> listener) {


	}

	@Override
	public SearchRequestBuilder prepareSearch(String... indices) {
		return new SearchRequestBuilder(new ElasticsearchClient() {

			@Override
			public ThreadPool threadPool() {
				return null;
			}

			@Override
			public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> RequestBuilder prepareExecute(
					Action<Request, Response, RequestBuilder> action) {
				return null;
			}

			@Override
			public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> void execute(
					Action<Request, Response, RequestBuilder> action, Request request, ActionListener<Response> listener) {
			}

			@Override
			public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> ActionFuture<Response> execute(
					Action<Request, Response, RequestBuilder> action, Request request) {
				return null;
			}
		}, SearchAction.INSTANCE);
	}

	@Override
	public ActionFuture<SearchResponse> searchScroll(SearchScrollRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void searchScroll(SearchScrollRequest request, ActionListener<SearchResponse> listener) {


	}

	@Override
	public SearchScrollRequestBuilder prepareSearchScroll(String scrollId) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<MultiSearchResponse> multiSearch(MultiSearchRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void multiSearch(MultiSearchRequest request, ActionListener<MultiSearchResponse> listener) {


	}

	@Override
	public MultiSearchRequestBuilder prepareMultiSearch() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<TermVectorsResponse> termVectors(TermVectorsRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void termVectors(TermVectorsRequest request, ActionListener<TermVectorsResponse> listener) {


	}

	@Override
	public TermVectorsRequestBuilder prepareTermVectors() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public TermVectorsRequestBuilder prepareTermVectors(String index, String type, String id) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<TermVectorsResponse> termVector(TermVectorsRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void termVector(TermVectorsRequest request, ActionListener<TermVectorsResponse> listener) {


	}

	@Override
	public TermVectorsRequestBuilder prepareTermVector() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public TermVectorsRequestBuilder prepareTermVector(String index, String type, String id) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<MultiTermVectorsResponse> multiTermVectors(MultiTermVectorsRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void multiTermVectors(MultiTermVectorsRequest request, ActionListener<MultiTermVectorsResponse> listener) {


	}

	@Override
	public MultiTermVectorsRequestBuilder prepareMultiTermVectors() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<PercolateResponse> percolate(PercolateRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void percolate(PercolateRequest request, ActionListener<PercolateResponse> listener) {


	}

	@Override
	public PercolateRequestBuilder preparePercolate() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<MultiPercolateResponse> multiPercolate(MultiPercolateRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void multiPercolate(MultiPercolateRequest request, ActionListener<MultiPercolateResponse> listener) {


	}

	@Override
	public MultiPercolateRequestBuilder prepareMultiPercolate() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ExplainRequestBuilder prepareExplain(String index, String type, String id) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<ExplainResponse> explain(ExplainRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void explain(ExplainRequest request, ActionListener<ExplainResponse> listener) {


	}

	@Override
	public ClearScrollRequestBuilder prepareClearScroll() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<ClearScrollResponse> clearScroll(ClearScrollRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void clearScroll(ClearScrollRequest request, ActionListener<ClearScrollResponse> listener) {


	}

	@Override
	public FieldStatsRequestBuilder prepareFieldStats() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public ActionFuture<FieldStatsResponse> fieldStats(FieldStatsRequest request) {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public void fieldStats(FieldStatsRequest request, ActionListener<FieldStatsResponse> listener) {


	}

	@Override
	public Settings settings() {

		throw new UnsupportedOperationException("not implemented in test");
	}

	@Override
	public Headers headers() {

		throw new UnsupportedOperationException("not implemented in test");
	}

}
