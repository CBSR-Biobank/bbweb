/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./ceventSpecimensView.html'),
  controller: CeventSpecimensViewController,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    participant:         '<',
    collectionEventType: '<',
    collectionEvent:     '<'
  }
};

/* @ngInject */
function CeventSpecimensViewController($q,
                                       $state,
                                       gettextCatalog,
                                       Specimen,
                                       Centre,
                                       specimenAddModal,
                                       domainNotificationService,
                                       notificationsService,
                                       resourceErrorService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.specimens       = [];
    vm.centreLocations = [];
    vm.tableController = undefined;

    vm.addSpecimens    = addSpecimens;
    vm.getTableData    = getTableData;
    vm.removeSpecimen  = removeSpecimen;
    vm.viewSpecimen    = viewSpecimen;
  }

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
                                     vm.collectionEvent.collectionEventType.specimenDescriptions,
                                     new Date(vm.collectionEvent.timeCompleted)).result;
      })
      .then(function (specimens) {
        return Specimen.add(vm.collectionEvent.id, specimens)
          .then(function () {
            notificationsService.success(gettextCatalog.getString('Specimen added'));
            reloadTableData();
          })
          .catch(function (err) {
            notificationsService.error(JSON.stringify(err));
          });
      });
  }

  function getTableData(tableState, controller) {
    var pagination    = tableState.pagination,
        sortPredicate = tableState.sort.predicate || 'inventoryId',
        sortOrder     = tableState.sort.reverse || false,
        options = {
          sort:     sortPredicate,
          page:     1 + (pagination.start / vm.limit),
          limit: vm.limit,
          order:    sortOrder ? 'desc' : 'asc'
        };

    if (!vm.tableController && controller) {
      vm.tableController = controller;
    }

    vm.tableDataLoading = true;

    Specimen.list(vm.collectionEvent.id, options)
      .then(function (paginatedSpecimens) {
        vm.specimens = paginatedSpecimens.items;
        tableState.pagination.numberOfPages = paginatedSpecimens.maxPages;
        vm.tableDataLoading = false;
      })
      .catch(resourceErrorService.checkUnauthorized());
  }

  function reloadTableData() {
    getTableData(vm.tableController.tableState());
  }

  function viewSpecimen(specimen) {
    $state.go('home.collection.study.participant.cevents.details.specimen',
              { specimenSlug: specimen.slug });
  }

  function removeSpecimen(specimen) {
    domainNotificationService.removeEntity(
      promiseFn,
      gettextCatalog.getString('Remove specimen'),
      gettextCatalog.getString(
        'Are you sure you want to remove specimen with inventory ID <strong>{{id}}</strong>?',
        { id: specimen.inventoryId }),
      gettextCatalog.getString('Remove failed'),
      gettextCatalog.getString(
        'Specimen with ID {{id}} cannot be removed',
        { id: specimen.inventoryId }));

    function promiseFn() {
      return specimen.remove(vm.collectionEvent.id).then(function () {
        notificationsService.success(gettextCatalog.getString('Specimen removed'));
        reloadTableData();
      });
    }
  }

}

export default ngModule => ngModule.component('ceventSpecimensView', component)
