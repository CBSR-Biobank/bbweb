/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * Uses Toastr to display notifications in pop up dialog boxes.
 */
class NotificationsService {

  constructor($q, gettextCatalog, toastr) {
    'ngInject'
    Object.assign(this, { $q, gettextCatalog, toastr })
  }

  submitSuccess() {
    this.toastr.success('Your changes were saved.');
  }

  success(message, title, timeout = 1500) {
    var options = {
      closeButton:     true,
      timeOut:         timeout,
      extendedTimeOut: 0
    };

    this.toastr.success(message, title, options);
  }

  error(message, title, timeout = 0) {
    var options = {
      closeButton:     true,
      timeOut:         timeout,
      extendedTimeOut: (timeout > 0) ? timeout * 2 : 0,
      positionClass:   'toast-bottom-right'
    };

    this.toastr.error(message, title, options);
  }

  /**
   * Error is the error returned from a biobankApiService call that failed.
   */
  updateError(err) {
    var message,
        title   = this.gettextCatalog.getString('Cannot apply your change');
    if (err.message) {
      message = err.message;
    } else {
      message = this.gettextCatalog.getString('Your change could not be saved');
    }
    this.error(message, title);
  }

}

export default ngModule => ngModule.service('notificationsService', NotificationsService)
