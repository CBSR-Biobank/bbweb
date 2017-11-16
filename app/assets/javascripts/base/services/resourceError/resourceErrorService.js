/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 *
 */

/**
 *
 */
class ResourceErrorService {

  constructor($q, $log, $state, userService, gettextCatalog) {
    Object.assign(this, { $q, $log, $state, userService, gettextCatalog })
  }

  // returns a function that can be called from a promise's catch clause
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

  // returns a function that can be called from a promise's catch clause
  goto404(msg) {
    return (err) => {
      const errMessage = `${msg}: ${err.message}`
      this.$log.error(errMessage)
      this.$state.go('404', { errMessage }, { location: false })
      return this.$q.reject(`status: ${err.status}:  ${err.message}`)
    }
  }

}

export default ngModule => ngModule.service('resourceErrorService', ResourceErrorService)
