/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   * Displays the study name and description and allows the user to change the status of the study.
   */
  function studySummaryDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/studySummary/studySummary.html',
      controller: StudySummaryCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  StudySummaryCtrl.$inject = [
    '$scope',
    '$state',
    'modalService',
    'modalInput',
    'notificationsService',
    'CollectionEventType'
  ];

  function StudySummaryCtrl($scope,
                            $state,
                            modalService,
                            modalInput,
                            notificationsService,
                            CollectionEventType) {

    var vm = this,
        validStatusActions = ['disable', 'enable', 'retire', 'unretire'];

    vm.descriptionToggleLength = 100;
    vm.changeStatus            = changeStatus;
    vm.editName                = editName;
    vm.editDescription         = editDescription;

    init();

    //--

    function init() {
      // updates the selected tab in 'studyViewDirective' which is the parent directive
      $scope.$emit('study-view', 'study-summary-selected');

      CollectionEventType.list(vm.study.id).then(function (ceTypes) {
        var specimenSpecs = _.flatMap(ceTypes, function(ceType) { return ceType.specimenSpecs; });
        vm.hasSpecimenSpecs = (specimenSpecs.length > 0);
      });
    }

    function changeStatus(statusAction) {
      var modalOptions = {
        closeButtonText: 'Cancel',
        headerHtml: 'Confirm study ',
        bodyHtml: 'Are you sure you want to '
      };

      if (!_.includes(validStatusActions, statusAction)) {
        throw new Error('invalid status: ' + statusAction);
      }

      modalOptions.headerHtml += statusAction;
      modalOptions.bodyHtml += statusAction + ' study ' + vm.study.name + '?';

      modalService.showModal({}, modalOptions).then(function () {
        return vm.study[statusAction]();
      }).then(function (study) {
        vm.study = study;
        notificationsService.success('The study\'s status has been updated.', null, 2000);
      });
    }

    function postUpdate(message, title, timeout) {
      return function (study) {
        vm.study = study;
        notificationsService.success(message, title, timeout);
      };
    }

    function editName() {
      modalInput.text('Edit name',
                      'Name',
                      vm.study.name,
                      { required: true, minLength: 2 })
        .result.then(function (name) {
          vm.study.updateName(name)
            .then(postUpdate('Name changed successfully.',
                             'Change successful',
                             1500))
            .catch(notificationsService.updateError);
        });
    }

    function editDescription() {
      modalInput.textArea('Edit description', 'Description', vm.study.description)
        .result.then(function (description) {
          vm.study.updateDescription(description)
            .then(postUpdate('Description changed successfully.',
                             'Change successful',
                             1500))
            .catch(notificationsService.updateError);
        });
    }
  }

  return studySummaryDirective;

});
