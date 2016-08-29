/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['toastr'], function(toastr) {
  'use strict';

  notificationsService.$inject = ['gettextCatalog'];

  /**
   *
   */
  function notificationsService(gettextCatalog) {
    var service = {
      submitSuccess: submitSuccess,
      success:       success,
      error:         error,
      updateError:   updateError
    };
    return service;

    //-------

    function submitSuccess() {
      toastr.options.positionClass = 'toast-bottom-right';
      toastr.success('Your changes were saved.');
    }

    function success(message, title, timeout) {
      var options = {
        closeButton: true,
        timeOut:  timeout || 1500,
        extendedTimeOut: 0,
        positionClass: 'toast-bottom-right'
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
      var message = gettextCatalog.getString('Your change could not be saved'),
          title   = gettextCatalog.getString('Cannot apply your change');
      if (err.data) {
        message += ': ' + err.data.message;
      }
      error(message, title);
    }

  }

  return notificationsService;
});
