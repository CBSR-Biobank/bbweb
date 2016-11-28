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
    'gettextCatalog',
    'modalService',
    'modalInput',
    'notificationsService'
  ];

  function CentreSummaryCtrl($scope,
                             $filter,
                             gettextCatalog,
                             modalService,
                             modalInput,
                             notificationsService) {
    var vm = this;
    vm.descriptionToggleControl = {}; // for truncateToggle directive
    vm.descriptionToggleState   = true;
    vm.descriptionToggleLength  = 100;

    vm.changeState    = changeState;
    vm.editName        = editName;
    vm.editDescription = editDescription;

    init();

    //----

    function init() {
      // updates the selected tab in 'studyViewDirective' which is the parent directive
      $scope.$emit('centre-view', 'centre-summary-selected');
    }

    function changeState(state) {
      var changeStateFn, stateChangeMsg;

      if (state === 'enable') {
        changeStateFn = vm.centre.enable;
        stateChangeMsg = gettextCatalog.getString('Are you sure you want to enable centre {{name}}?',
                                                   { name: vm.centre.name });
      } else if (state === 'disable') {
        changeStateFn = vm.centre.disable;
        stateChangeMsg = gettextCatalog.getString('Are you sure you want to disable centre {{name}}?',
                                                   { name: vm.centre.name });
      } else {
        throw new Error('invalid state: ' + state);
      }

      modalService.modalOkCancel(gettextCatalog.getString('Confirm state change on centre'),
                                 stateChangeMsg)
        .then(function () {
          _.bind(changeStateFn, vm.centre)().then(function (centre) {
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
      modalInput.text(gettextCatalog.getString('Edit name'),
                      gettextCatalog.getString('Name'),
                      vm.centre.name,
                      { required: true, minLength: 2 }).result
        .then(function (name) {
          vm.centre.updateName(name)
            .then(postUpdate(gettextCatalog.getString('Name changed successfully.'),
                             gettextCatalog.getString('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editDescription() {
      modalInput.textArea(gettextCatalog.getString('Edit description'),
                          gettextCatalog.getString('Description'),
                          vm.centre.description).result
        .then(function (description) {
          vm.centre.updateDescription(description)
            .then(postUpdate(gettextCatalog.getString('Description changed successfully.'),
                             gettextCatalog.getString('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

  }

  return centreSummaryDirective;
});
