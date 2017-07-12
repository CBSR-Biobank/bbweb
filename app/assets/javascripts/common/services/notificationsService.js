/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function() {
  'use strict';

  notificationsService.$inject = ['$q', 'gettextCatalog', 'toastr'];

  /**
   * Uses Toastr to display notifications in pop up dialog boxes.
   */
  function notificationsService($q, gettextCatalog, toastr) {
    var service = {
      submitSuccess: submitSuccess,
      success:       success,
      error:         error,
      updateError:   updateError
    };
    return service;

    //-------

    function submitSuccess() {
      toastr.success('Your changes were saved.');
    }

    function success(message, title, timeout) {
      var options = {
        closeButton: true,
        timeOut:  timeout || 1500,
        extendedTimeOut: 0
      };

      toastr.success(message, title, options);
    }

    function error(message, title, _timeout) {
      var timeout = _timeout || 0;
      var options = {
        closeButton: true,
        timeOut:  timeout,
        extendedTimeOut: (timeout > 0) ? timeout * 2 : 0,
        positionClass: 'toast-bottom-right'
      };

      toastr.error(message, title, options);
    }

    /**
     * Error is the error returned from a biobankApiService call that failed.
     */
    function updateError(err) {
      var message,
          title   = gettextCatalog.getString('Cannot apply your change');
      if (err.message) {
        message = err.message;
      } else {
        message = gettextCatalog.getString('Your change could not be saved');
      }
      error(message, title);
    }

  }

  return notificationsService;
});
