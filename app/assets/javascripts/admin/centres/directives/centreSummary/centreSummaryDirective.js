/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
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
      templateUrl : '/assets/javascripts/admin/centres/directives/centreSummary/centreSummary.html',
      controller: CentreSummaryCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CentreSummaryCtrl.$inject = [
    '$scope',
    '$filter',
    'gettext',
    'gettextCatalog',
    'modalService',
    'modalInput',
    'notificationsService'
  ];

  function CentreSummaryCtrl($scope,
                             $filter,
                             gettext,
                             gettextCatalog,
                             modalService,
                             modalInput,
                             notificationsService) {
    var vm = this;
    vm.descriptionToggleControl = {}; // for truncateToggle directive
    vm.descriptionToggleState   = true;
    vm.descriptionToggleLength  = 100;

    vm.changeStatus    = changeStatus;
    vm.editName        = editName;
    vm.editDescription = editDescription;

    init();

    //----

    function init() {
      // updates the selected tab in 'studyViewDirective' which is the parent directive
      $scope.$emit('centre-view', 'centre-summary-selected');
    }

    function changeStatus(status) {
      var changeStatusFn, statusChangeMsg;

      if (status === 'enable') {
        changeStatusFn = vm.centre.enable;
        statusChangeMsg = gettextCatalog.getString('Are you sure you want to enable centre {{name}}?',
                                                   { name: vm.centre.name });
      } else if (status === 'disable') {
        changeStatusFn = vm.centre.disable;
        statusChangeMsg = gettextCatalog.getString('Are you sure you want to disable centre {{name}}?',
                                                   { name: vm.centre.name });
      } else {
        throw new Error('invalid status: ' + status);
      }

      modalService.modalOkCancel(gettext('Confirm status change on centre', statusChangeMsg))
        .then(function () {
          _.bind(changeStatusFn, vm.centre)().then(function (centre) {
            vm.centre = centre;
          });
      });
    }

    function postUpdate(message, title, timeout) {
      timeout = timeout || 1500;
      return function (centre) {
        vm.centre = centre;
        notificationsService.success(message, title, timeout);
      };
    }

    function editName() {
      modalInput.text(gettext('Edit name'),
                      gettext('Name'),
                      vm.centre.name,
                      { required: true, minLength: 2 }).result
        .then(function (name) {
          vm.centre.updateName(name)
            .then(postUpdate(gettext('Name changed successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editDescription() {
      modalInput.textArea(gettext('Edit description'),
                          gettext('Description'),
                          vm.centre.description).result
        .then(function (description) {
          vm.centre.updateDescription(description)
            .then(postUpdate(gettext('Description changed successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

  }

  return centreSummaryDirective;
});
