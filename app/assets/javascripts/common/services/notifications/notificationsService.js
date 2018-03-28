/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * Uses Toastr to display notifications in pop up dialog boxes.
 *
 * @memberOf common.services
 */
class NotificationsService {

  constructor($q, gettextCatalog, toastr) {
    'ngInject'
    Object.assign(this, { $q, gettextCatalog, toastr })
  }

  submitSuccess() {
    this.toastr.success('Your changes were saved.');
  }

  /**
   * Displays an success in the browser as a temporary popup dialog box.
   *
   * Success messages have a green background.
   *
   * @param {string} message The message to display.
   *
   * @param {string} title The title to display.
   *
   * @param {int} timeout the amout of time to display the dialog box for.
   */
  success(message, title, timeout = 1500) {
    var options = {
      closeButton:     true,
      timeOut:         timeout,
      extendedTimeOut: 0
    };

    this.toastr.success(message, title, options);
  }

  /**
   * Displays an error in the browser as a temporary popup dialog box.
   *
   * Error messages have a red background.
   *
   * @param {string} message The message to display.
   *
   * @param {string} title The title to display.
   *
   * @param {int} timeout the amout of time to display the dialog box for.
   */
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
   * Displays an error in the browser as a temporary popup dialog box.
   *
   * Meant to be called when a change to a domain entity fails.
   *
   * @param {string} err The error message to display.
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
