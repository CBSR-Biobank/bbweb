/**
 * AngularJS Component for used in {@link domain.studies.ProcessingType ProcessingType}
 * administration.
 *
 * @namespace admin.studies.components.processingTypesAddAndSelect
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

class ProcessingTypesAddAndSelectController {

  constructor($scope,
              $state,
              gettextCatalog,
              ProcessingType) {
    'ngInject';
    Object.assign(this,
                  {
                    $scope,
                    $state,
                    gettextCatalog,
                    ProcessingType
                  });
  }

  $onInit() {
    this.displayStates = {
      NO_RESULTS: 0,
      HAVE_RESULTS: 1,
      NOT_CONFIGURED: 2
    };

    this.pagerOptions = {
      page:   1,
      limit:  5,
      sortField: 'name'
    };

    this.nameFilter        = '';
    this.displayState      = this.displayStates.NOT_CONFIGURED;
    this.updateProcessingTypes();
  }

  updateProcessingTypes() {
    this.ProcessingType.list(this.study.slug, this.pagerOptions)
      .then(pagedResult => {
        this.pagedResult          = pagedResult;
        this.processingTypes      = this.ProcessingType.sortByName(pagedResult.items);
        this.displayState         = this.getDisplayState();
        this.showPagination       = this.getShowPagination();
        this.paginationNumPages   = pagedResult.maxPages;
      });
  }

  getDisplayState() {
    if (this.pagedResult.total > 0) {
      return this.displayStates.HAVE_RESULTS;
    }
    return (this.nameFilter === '') ? this.displayStates.NOT_CONFIGURED: this.displayStates.NO_RESULTS;
  }

  getShowPagination() {
    return (this.displayState === this.displayStates.HAVE_RESULTS) && (this.pagedResult.maxPages > 1);
  }

  pageChanged() {
    this.updateProcessingTypes();
    this.$state.go('home.admin.studies.study.processing');
  }

  add() {
    this.$state.go('home.admin.studies.study.processing.addType.information');
  }

  select(processingType) {
    this.$state.go('home.admin.studies.study.processing.viewType',
              { processingTypeSlug: processingType.slug });
  }

  nameFilterUpdated() {
    if (this.nameFilter) {
      this.pagerOptions.filter = 'name:like:' + this.nameFilter;
    } else {
      this.pagerOptions.filter = '';
    }
    this.pagerOptions.page = 1;
    this.pageChanged();
  }
}

/**
 * An AngularJS component that displays all the {@link domain.studies.ProcessingType
 * ProcessingTypes} for a {@link domain.studies.Study Study} and allows the user to select one.
 *
 * @memberOf admin.studies.components.processingTypesAddAndSelect
 *
 * @param {domain.studies.Study} study - the study the processing types belongs to.
 *
 * @param {Array<domain.studies.ProcessingType>} processingTypes - the processing types the
 * study has.
 */
const processingTypesAddAndSelectComponent = {
  template: require('./processingTypesAddAndSelect.html'),
  controller: ProcessingTypesAddAndSelectController,
  controllerAs: 'vm',
  bindings: {
    study:           '<',
    processingTypes: '<',
    addAllowed:      '<'
  }
}

export default ngModule => ngModule.component('processingTypesAddAndSelect',
                                             processingTypesAddAndSelectComponent)
