/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
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
 * Makes a request to the Biobank server REST API. All REST responses from the server have a similar
 * response JSON object. This service returns the 'data' field if the call was successful.
 *
 * @return {object} An AngularJS service.
 */
class biobankApiService {

  constructor($http, $q, $log, AppConfig) {
    Object.assign(this, { $http, $q, $log, AppConfig })
  }

  get(url, params) {
    return apiCall.bind(this)('GET', url, { params: params });
  }

  post(url, data) {
    return apiCall.bind(this)('POST', url, { data: data });
  }

  put(url, data) {
    return apiCall.bind(this)('PUT', url, { data: data });
  }

  del(url) {
    return apiCall.bind(this)('DELETE', url);
  }

  // this function taken from here:
  // https://gist.github.com/mathewbyrne/1280286
  slugify(text) {
    return text.toString().toLowerCase().trim()
      .replace(/[^\w\s-]/g, '') // remove non-word [a-z0-9_], non-whitespace, non-hyphen characters
      .replace(/[\s_-]+/g, '_') // swap any length of whitespace, underscore, hyphen characters with a single _
      .replace(/^-+|-+$/g, ''); // remove leading, trailing -
  }
}

export default ngModule => ngModule.service('biobankApi', biobankApiService)
