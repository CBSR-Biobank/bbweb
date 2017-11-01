/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 *
 */

/**
 *
 */
class ResourceErrorService {

  constructor($log, $state) {
    Object.assign(this, { $log, $state })
  }

  goto404(msg) {
    return (err) => {
      const errMessage = `${msg}: ${err.message}`
      this.$log.error(errMessage)
      this.$state.go('404', { errMessage }, { location: false })
    }
  }

}

export default ngModule => ngModule.service('resourceErrorService', ResourceErrorService)
