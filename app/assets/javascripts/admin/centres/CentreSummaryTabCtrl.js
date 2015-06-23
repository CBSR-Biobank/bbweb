/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  CentreSummaryTabCtrl.$inject = ['$filter', 'modalService', 'centre'];

  /**
   * Displays the centre administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the centre.
   */
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
        changeStatusFn = vm.centre.enable;
      } else if (status === 'disable') {
        changeStatusFn = vm.centre.disable;
      } else {
        throw new Error('invalid status: ' + status);
      }

      modalOptions.headerHtml += status;
      modalOptions.bodyHtml += status + ' centre ' + vm.centre.name + '?';

      modalService.showModal({}, modalOptions).then(function () {
        _.bind(changeStatusFn, vm.centre)().then(function (centre) {
          vm.centre = centre;
        });
      });
    }

  }

  return CentreSummaryTabCtrl;
});
