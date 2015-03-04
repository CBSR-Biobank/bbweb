/**
 * Centre administration controllers.
 */
define(['../module', 'underscore'], function(module, _) {
  'use strict';

  /**
   * Displays the centre administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the centre.
   */
  module.controller('CentreSummaryTabCtrl', CentreSummaryTabCtrl);

  CentreSummaryTabCtrl.$inject = ['$filter', 'modalService', 'centre'];

  function CentreSummaryTabCtrl($filter, modalService, centre) {
    var vm = this;
    vm.centre = centre;
    vm.descriptionToggleControl = {}; // for truncateToggle directive
    vm.descriptionToggleState = true;
    vm.descriptionToggleLength = 100;

    vm.changeStatus = changeStatus;

    //----

    function changeStatus(status) {
      var changeStatusFn;
      var modalOptions = {
        closeButtonText: 'Cancel',
        headerHtml: 'Confirm centre ',
        bodyHtml: 'Are you sure you want to '
      };

      if (status === 'enable') {
        changeStatusFn = centre.enable;
      } else if (status === 'disable') {
        changeStatusFn = centre.disable;
      } else {
        throw new Error('invalid status: ' + status);
      }

      modalOptions.headerHtml += status;
      modalOptions.bodyHtml += status + ' centre ' + vm.centre.name + '?';

      modalService.showModal({}, modalOptions).then(function () {
        _.bind(changeStatusFn, centre)().then(function (centre) {
          vm.centre = centre;
        });
      });
    }

  }

});
