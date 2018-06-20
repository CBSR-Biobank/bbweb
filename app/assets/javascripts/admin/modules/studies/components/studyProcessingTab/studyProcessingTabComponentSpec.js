/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ProcessingTypeFixture from 'test/fixtures/ProcessingTypeFixture';
import ngModule from '../../index';

describe('studyProcessingTabComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);
      this.injectDependencies('$httpBackend',
                              'Study',
                              'CollectionEventType',
                              'ProcessingType',
                              'Factory');

      this.processingTypeFixture = new ProcessingTypeFixture(this.Factory,
                                                             this.Study,
                                                             this.CollectionEventType,
                                                             this.ProcessingType);
      this.createController =
        (study,
         plainEventTypes = [],
         plainProcessingTypes = []
        ) => {
          this.$httpBackend
            .expectGET(this.url('studies/cetypes', study.slug))
            .respond(this.reply(this.Factory.pagedResult(plainEventTypes)));

          // child component requests these
          this.$httpBackend
            .expectGET(new RegExp(this.url('studies/proctypes', study.slug)))
            .respond(this.reply(this.Factory.pagedResult(plainProcessingTypes)));

          this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
          this.$rootScope.$on('tabbed-page-update', this.eventRxFunc);

          this.createControllerInternal(
            '<study-processing-tab study="vm.study"></study-processing-tab>',
            { study },
            'studyProcessingTab');
          this.$httpBackend.flush();
        };
    });
  });

  it('initialization is valid', function() {
    const f = this.processingTypeFixture.fixture();
    const plainEventTypes = [ f.eventTypes[0].plainEventType ];
    const plainProcessingTypes = [ f.processingTypesFromProcessed[0].plainProcessingType ];

    this.createController(f.study, plainEventTypes, plainProcessingTypes);
    expect(this.controller.study).toBe(f.study);
    expect(this.eventRxFunc).toHaveBeenCalled();
    expect(this.controller.processingTypes).toBeEmptyArray();
    expect(this.controller.haveCollectionEventTypes).toBe(plainEventTypes.length > 0);
  });

  it('should update the processing types when event is emitted', function() {
    const f = this.processingTypeFixture.fixture();
    const plainEventTypes = [ f.eventTypes[0].plainEventType ];
    const plainProcessingTypes = [ f.processingTypesFromProcessed[0].plainProcessingType ];
    const processingType = f.processingTypesFromProcessed[0].processingType;
    const newName = this.Factory.stringNext();

    this.createController(f.study, plainEventTypes, plainProcessingTypes);
    const childScope = this.element.isolateScope().$new();
    processingType.name = newName;
    childScope.$emit('processing-type-updated', processingType);
    this.scope.$digest();

    expect(this.controller.processingTypes).toBeArrayOfSize(1);
    expect(this.controller.processingTypes[0].name).toBe(newName);
  });

});
