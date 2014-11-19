/**
 * Centre administration controllers.
 */
define(['../module'], function(module) {
  'use strict';

  /**
   * Displays the centre administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the centre.
   */
  module.controller('CentreSummaryTabCtrl', CentreSummaryTabCtrl);

  CentreSummaryTabCtrl.$inject = ['$filter', 'centresService', 'modalService', 'centre'];

  function CentreSummaryTabCtrl($filter, centresService, modalService, centre) {
    var vm = this;
    vm.centre = centre;
    vm.descriptionToggleControl = {}; // for truncateToggle directive
    vm.descriptionToggleState = true;
    vm.descriptionToggleLength = 100;

    vm.changeStatus = changeStatus;

    //----

    function changeStatus(status) {
      var serviceFn;
      var modalOptions = {
        closeButtonText: 'Cancel',
        headerHtml: 'Confirm centre ',
        bodyHtml: 'Are you sure you want to '
      };

      if (status === 'enable') {
        serviceFn = centresService.enable;
      } else if (status === 'disable') {
        serviceFn = centresService.disable;
      } else {
        throw new Error('invalid status: ' + status);
      }

      modalOptions.headerHtml += status;
      modalOptions.bodyHtml += status + ' centre ' + vm.centre.name + '?';

      modalService.showModal({}, modalOptions).then(function () {
        serviceFn(vm.centre).then(function () {
          centresService.get(vm.centre.id).then(function (centre) {
            vm.centre = centre;
          });
        });
      });
    }

  }

});
