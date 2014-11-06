define(['../module'], function(module) {
  'use strict';

  module.controller('StudySummaryTabCtrl', StudySummaryTabCtrl);

  StudySummaryTabCtrl.$inject = ['$state', 'stateHelper', 'studiesService', 'modalService', 'study'];

  /**
   * Displays the study administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the study.
   */
  function StudySummaryTabCtrl($state, stateHelper, studiesService, modalService, study) {
    var vm = this;

    vm.study = study;
    vm.descriptionToggleLength = 100;
    vm.changeStatus = changeStatus;

    //--

    function changeStatus(status) {
      var serviceFn;
      var modalOptions = {
        closeButtonText: 'Cancel',
        headerHtml: 'Confirm study ',
        bodyHtml: 'Are you sure you want to '
      };

      if (status === 'enable') {
        serviceFn = studiesService.enable;
      } else if (status === 'disable') {
        serviceFn = studiesService.disable;
      } else if (status === 'retire') {
        serviceFn = studiesService.retire;
      } else if (status === 'unretire') {
        serviceFn = studiesService.unretire;
      } else {
        throw new Error('invalid status: ' + status);
      }

      modalOptions.headerHtml += status;
      modalOptions.bodyHtml += status + ' study ' + vm.study.name + '?';

      modalService.showModal({}, modalOptions).then(function () {
        serviceFn(vm.study).then(function () {
          studiesService.get(vm.study.id).then(function (study) {
            vm.study = study;
          });
        });
      });
    }

  }

});
