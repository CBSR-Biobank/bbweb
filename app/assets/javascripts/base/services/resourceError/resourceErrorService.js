/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 *
 */

/**
 * An AngularJS Service that changes state to a 404 page and displays a message.
 *
 * @memberOf base.services
 */
class ResourceErrorService {

  constructor($q, $log, $state, userService, gettextCatalog) {
    'ngInject'
    Object.assign(this, { $q, $log, $state, userService, gettextCatalog })
  }

  /**
   * Returns a function that handles a rejected promise from a call to one of the methods in {@link
   * ng.base.services.BiobankApiService BiobankApiService}.
   *
   * @return {base.services.ErrorService.CheckRejectedRequest} Returns a function that can be called from a
   * promise's catch clause..
   */
  checkUnauthorized() {
    return (err) => {
      if (err.status === 401) {
        this.userService.retrieveCurrentUser()
        const errMessage = this.gettextCatalog.getString('You are not authorized to do that')
        this.$state.go('404', { errMessage }, { location: false })
        return this.$q.reject('User is unauthorized')
      }
      this.$state.go('404', { errMessage: err.message }, { location: false })
      return this.$q.reject(`status: ${err.status}:  ${err.message}`)
    }
  }

  /**
   * Returns a function that can be called from a promise's catch clause.
   *
   * @return {function} the function to bind to the promise.
   */
  goto404(msg) {
    return (err) => {
      const errMessage = `${msg}: ${err.message}`
      this.$log.error(errMessage)
      this.$state.go('404', { errMessage }, { location: false })
      return this.$q.reject(`status: ${err.status}:  ${err.message}`)
    }
  }

}

/**
 * Changes the application's state to the `404` state and displays an appropriate message based on the status
 * code.
 *
 * @callback base.services.ResourceErrorService.CheckRejectedRequest
 *
 * @param {$httpResponseObject} err - the error returned by the $http request.
 */

export default ngModule => ngModule.service('resourceErrorService', ResourceErrorService)
