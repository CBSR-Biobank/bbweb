/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/collection/components/ceventSpecimensView/ceventSpecimensView.html',
    controller: CeventSpecimensViewController,
    controllerAs: 'vm',
    bindings: {
      study:           '<',
      collectionEvent: '<'
    }
  };

  CeventSpecimensViewController.$inject = [
    '$q',
    'Specimen',
    'Centre',
    'specimenAddModal',
    'domainEntityService',
    'notificationsService'
  ];

  /**
   *
   */
  function CeventSpecimensViewController($q,
                                         Specimen,
                                         Centre,
                                         specimenAddModal,
                                         domainEntityService,
                                         notificationsService) {
    var vm = this;

    vm.specimens       = [];
    vm.centreLocations = [];
    vm.tableController = undefined;

    vm.addSpecimens       = addSpecimens;
    vm.getTableData       = getTableData;
    vm.removeSpecimen     = removeSpecimen;

    //--

    function addSpecimens() {
      var defer = $q.defer();

      if (vm.centreLocations.length <= 0) {
        vm.study.allLocations().then(function (centreLocations) {
          defer.resolve(Centre.centreLocationToNames(centreLocations));
        });
      } else {
        defer.resolve(vm.centreLocations);
      }

      defer.promise
        .then(function (centreLocations) {
          vm.centreLocations = centreLocations;
          return specimenAddModal.open(vm.centreLocations,
                                       vm.collectionEvent.collectionEventType.specimenSpecs).result;
        })
        .then(function (specimens) {
          return Specimen.add(vm.collectionEvent.id, specimens);
        })
        .then(function () {
          notificationsService.success('Specimen added');
          reloadTableData();
        });
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
      domainEntityService.removeEntity(
        promiseFn,
        'Remove specimen',
        'Are you sure you want to remove specimen with inventory ID <strong>' + specimen.inventoryId + '</strong>?',
        'Remove failed',
        'Specimen with ID ' + specimen.inventoryId + ' cannot be removed');

      function promiseFn() {
        return specimen.remove().then(function () {
          notificationsService.success('Specimen removed');
          reloadTableData();
        });
      }
    }

  }

  return component;
});
