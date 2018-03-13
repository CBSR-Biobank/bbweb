/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

function apiCall(method, url, config = {}) {
  if (url.indexOf(this.AppConfig.restApiUrlPrefix) < 0) {
    throw new Error('invalid REST API url: ' + url);
  }

  Object.assign(config, { method: method, url: url });

  return this.$http(config)
    .then(response => {
      // TODO: check status here and log it if it not 'success'
      if (response.data) {
        if (response.data.status === 'success'){
          return this.$q.when(response.data.data);
        }
        return this.$q.when(response.data);
      }
      return this.$q.when({});
    })
    .catch(response => {
      if (response.data) {
        this.$log.error(response.status, response.data.message);
        return this.$q.reject(response.data);
      }
      return this.$q.reject(response);
    });
}

/**
 * Makes a request to the Biobank server REST API.
 *
 * <p> All REST responses from the server have a similar response JSON object. The methods in this service
 * return the 'data' field if the call was successful.
 *
 * @memberOf base.services
 */
class BiobankApiService {

  constructor($http, $q, $log, AppConfig) {
    'ngInject'
    Object.assign(this, { $http, $q, $log, AppConfig })
  }

  /**
   * Sends a GET request to the server.
   *
   * @param {string} url - The resource that is being requested.
   *
   * @param {string} params - The query parameters to append to the resource.
   *
   * @return {Promise<object>} The object returned from the server.
   */
  get(url, params) {
    return apiCall.bind(this)('GET', url, { params: params });
  }

  /**
   * Sends a POST request to the server.
   *
   * @param {string} url - The resource that to add.
   *
   * @param {object} data - The JSON parameters to append to the request.
   *
   * @return {Promise<object>} The object returned from the server.
   */
  post(url, data) {
    return apiCall.bind(this)('POST', url, { data: data });
  }

  /**
   * Sends a PUT request to the server.
   *
   * @param {string} url - The resource to replace.
   *
   * @param {object} data - The JSON parameters to append to the request.
   *
   * @return {Promise<object>} The object returned from the server.
   */
  put(url, data) {
    return apiCall.bind(this)('PUT', url, { data: data });
  }

  /**
   * Sends a DELETE request to the server.
   *
   * @param {string} url - The resource to delete.
   *
   * @return {Promise<object>} The object returned from the server.
   */
  del(url) {
    return apiCall.bind(this)('DELETE', url);
  }

  /**
   * Utility function to generate a URL from an array of strings.
   *
   * The Biobank Server's REST API prefix is also prepended to the result.
   *
   * @param {...string} paths - the path elements to the REST API (without the slash character).
   *
   * @return {string} The URL.
   *
   * @example
   * // returns "/api/studies/study-id-1"
   * biobankApi.url("studies", "study-id-1");
   */
  url(...paths) {
    const args = [ this.AppConfig.restApiUrlPrefix ].concat(paths)
    if (args.length <= 0) {
      throw new Error('no arguments specified')
    }
    return args.join('/')
  }

  // this function taken from here:
  // https://gist.github.com/mathewbyrne/1280286
  /**
   * Generates a string that can be used in a URL from a regular string.
   *
   * @return {string} A string that can be used in a URL.
   */
  slugify(text) {
    return text.toString().toLowerCase().trim()
      .replace(/[^\w\s-]/g, '') // remove non-word [a-z0-9_], non-whitespace, non-hyphen characters
      .replace(/[\s_-]+/g, '_') // swap any length of whitespace, underscore, hyphen characters with a single _
      .replace(/^-+|-+$/g, ''); // remove leading, trailing -
  }
}

export default ngModule => ngModule.service('biobankApi', BiobankApiService)
