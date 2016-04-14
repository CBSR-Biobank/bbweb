/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  /**
   *
   */
  function ceventViewDirective() {
    var directive = {
      restrict: 'EA',
      scope: {},
      bindToController: {
        collectionEventTypes: '=',
        collectionEvent:      '='
      },
      templateUrl : '/assets/javascripts/collection/directives/ceventView/ceventView.html',
      controller: CeventViewCtrl,
      controllerAs: 'vm'
    };
    return directive;
  }

  CeventViewCtrl.$inject = [
    '$q',
    '$state',
    'Centre',
    'Specimen',
    'specimenAddModal',
    'timeService',
    'modalInput',
    'notificationsService',
    'annotationUpdate'
  ];

  /**
   *
   */
  function CeventViewCtrl($q,
                          $state,
                          Centre,
                          Specimen,
                          specimenAddModal,
                          timeService,
                          modalInput,
                          notificationsService,
                          annotationUpdate) {
    var vm = this;

    vm.specimens       = [];
    vm.centreLocations = [];
    vm.tableController = undefined;
    vm.canUpdateVisitType = (vm.collectionEventTypes.length > 1);

    vm.panelOpen          = true;
    vm.timeCompletedLocal = timeService.dateToDisplayString(vm.collectionEvent.timeCompleted);

    vm.editVisitType      = editVisitType;
    vm.editTimeCompleted  = editTimeCompleted;
    vm.editAnnotation     = editAnnotation;
    vm.addSpecimens       = addSpecimens;
    vm.panelButtonClicked = panelButtonClicked;
    vm.getTableData       = getTableData;
    vm.removeSpecimen     = removeSpecimen;

    //--

    function postUpdate(message, title, timeout) {
      return function (cevent) {
        vm.collectionEvent = cevent;
        vm.timeCompletedLocal = timeService.dateToDisplayString(vm.collectionEvent.timeCompleted);
        notificationsService.success(message, title, timeout);
      };
    }

    function editVisitType() {
      if (vm.collectionEventTypes.length <= 1) {
        throw new Error('only a single collection event type is defined for this study');
      }
    }

    function editTimeCompleted() {
      modalInput.dateTime('Update time completed',
                          'Time completed',
                          vm.timeCompletedLocal,
                          { required: true })
        .result.then(function (timeCompleted) {
          vm.collectionEvent.updateTimeCompleted(timeService.dateToUtcString(timeCompleted))
            .then(postUpdate('Time completed updated successfully.', 'Change successful', 1500))
            .catch(notificationsService.updateError);
        });
    }

    function editAnnotation(annotation) {
      annotationUpdate.update(annotation, 'Update ' + annotation.getLabel())
        .then(function (newAnnotation) {
          vm.collectionEvent.addAnnotation(newAnnotation)
            .then(postUpdate('Annotation updated successfully.', 'Change successful', 1500))
            .catch(notificationsService.updateError);
        });
    }

    function addSpecimens() {
      var defer = $q.defer();

      if (vm.centreLocations.length <= 0) {
        Centre.allLocations().then(function (centreLocations) {
          vm.centreLocations = centreLocations;
          defer.resolve();
        });
      } else {
        defer.resolve();
      }

      defer.promise
        .then(function () {
          return specimenAddModal.open(vm.centreLocations,
                                  vm.collectionEvent.collectionEventType.specimenSpecs).result;
        })
        .then(function (specimens) {
          return Specimen.add(vm.collectionEvent.id, specimens);
        })
        .then(reloadTableData);
    }

    function panelButtonClicked() {
      vm.panelOpen = !vm.panelOpen;
    }

    function getTableData(tableState, controller) {
      var pagination    = tableState.pagination,
          sortPredicate = tableState.sort.predicate || 'inventoryId',
          sortOrder     = tableState.sort.reverse || false,
          options = {
            sort:     sortPredicate,
            page:     1 + (pagination.start / vm.pageSize),
            pageSize: vm.pageSize,
            order:    sortOrder ? 'desc' : 'asc'
          };

      if (!vm.tableController && controller) {
        vm.tableController = controller;
      }

      vm.tableDataLoading = true;

      Specimen.list(vm.collectionEvent.id, options).then(function (paginatedUsers) {
        vm.specimens = paginatedUsers.items;
        tableState.pagination.numberOfPages = paginatedUsers.maxPages;
        vm.tableDataLoading = false;
      });
    }

    function reloadTableData() {
      getTableData(vm.tableController.tableState());
    }

    function removeSpecimen(specimen) {
      specimen.remove().then(reloadTableData);
    }

  }

  return ceventViewDirective;
});
