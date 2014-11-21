define(['../module', 'toastr'], function(module, toastr) {
  'use strict';

  module.service('notificationsService', notificationsService);

  notificationsService.$inject = [];

  /**
   *
   */
  function notificationsService() {
    var service = {
      submitSuccess: submitSuccess
    };
    return service;

    //-------

    function submitSuccess() {
      toastr.options.positionClass = 'toast-bottom-right';
      toastr.success('Your changes were saved.');
    }

  }

});
