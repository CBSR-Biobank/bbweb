/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  /**
   * Displays the centre administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the centre.
   */
  function centreSummaryDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        centre: '='
      },
      templateUrl : '/assets/javascripts/admin/directives/centres/centreSummary/centreSummary.html',
      controller: CentreSummaryCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CentreSummaryCtrl.$inject = ['$filter', 'modalService', 'modalInput', 'notificationsService'];

  function CentreSummaryCtrl($filter, modalService, modalInput, notificationsService) {
    var vm = this;
    vm.descriptionToggleControl = {}; // for truncateToggle directive
    vm.descriptionToggleState = true;
    vm.descriptionToggleLength = 100;

    vm.changeStatus = changeStatus;
    vm.editName = editName;
    vm.editDescription = editDescription;

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

    function postUpdate(message, title, timeout) {
      return function (centre) {
        vm.centre = centre;
        notificationsService.success(message, title, timeout);
      };
    }

    function editName() {
      modalInput.text('Edit name',
                      'Name',
                      vm.centre.name,
                      { required: true, minLength: 2 })
        .result.then(function (name) {
          vm.centre.updateName(name)
            .then(postUpdate('Name changed successfully.',
                             'Change successful',
                             1500))
            .catch(notificationsService.updateError);
        });
    }

    function editDescription() {
      modalInput.textArea('Edit description', 'Description', vm.centre.description)
        .result.then(function (description) {
          vm.centre.updateDescription(description)
            .then(postUpdate('Description changed successfully.',
                             'Change successful',
                             1500))
            .catch(notificationsService.updateError);
        });
    }

  }

  return centreSummaryDirective;
});
