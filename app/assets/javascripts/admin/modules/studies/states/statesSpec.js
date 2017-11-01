/**
 * Jasmine test suite
 *
 */
/* global angular */

import ngModule from '../../../../app'  // the whole appliction has to be loaded for these states

describe('collection states', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test', function ($qProvider) {
      // this is needed to make promis rejections work for a state's resolves
      $qProvider.errorOnUnhandledRejections(false);
    })

    angular.mock.inject(function(StateTestSuiteMixin) {
      Object.assign(this, StateTestSuiteMixin)

      this.injectDependencies('$q',
                              '$rootScope',
                              '$location',
                              '$httpBackend',
                              '$state',
                              'Study',
                              'CollectionEventType',
                              'Factory')

      this.study = this.Study.create(this.Factory.study())
    })
  })

  it('when navigating to `/admin/studies` should go to valid state', function () {
    this.gotoUrl('/admin/studies')
    expect(this.$state.current.name).toBe('home.admin.studies')
  })

  it('when navigating to `/admin/studies/add` should go to valid state', function () {
    this.gotoUrl('/admin/studies/add')
    expect(this.$state.current.name).toBe('home.admin.studies.add')
  })

  describe('when navigating to `/admin/studies/study-id-1/summary`', function () {
    const context = {}

    beforeEach(function() {
        context.childState = 'summary'
        context.url = '/admin/studies/study-id-1/summary'
      });

      studySharedBehaviour(context)
  })

  describe('when navigating to `/admin/studies/study-id-1/participants`', function () {
    const context = {}

    beforeEach(function() {
        context.childState = 'participants'
        context.url = '/admin/studies/study-id-1/participants'
      });

      studySharedBehaviour(context)
  })

  describe('when navigating to `/admin/studies/study-id-1/collection`', function () {
    const context = {}

    beforeEach(function() {
        context.childState = 'collection'
        context.url = '/admin/studies/study-id-1/collection'
      });

      studySharedBehaviour(context)
  })

  describe('when navigating to `/admin/studies/study-id-1/processing`', function () {
    const context = {}

    beforeEach(function() {
        context.childState = 'processing'
        context.url = '/admin/studies/study-id-1/processing'
      });

      studySharedBehaviour(context)
  })

  it('when navigating to `/admin/studies/study-id-1/participants/annottype/add` should go to valid state',
     function () {
       this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when({}))
       this.gotoUrl('/admin/studies/study-id-1/participants/annottype/add')
       expect(this.$state.current.name).toBe('home.admin.studies.study.participants.annotationTypeAdd')
     })

  describe(
    'when navigating to `/admin/studies/study-id-1/participants/annottype/annottype-id-1`',
    function () {

      it('should go to valid state', function() {
        const jsonAnnotType = this.Factory.annotationType(),
              jsonStudy     = this.Factory.study({ annotationTypes: [ jsonAnnotType ]}),
              study         = this.Study.create(jsonStudy)
        this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when(study))
        this.gotoUrl(`/admin/studies/study-id-1/participants/annottype/${jsonAnnotType.id}`)
        expect(this.$state.current.name).toBe('home.admin.studies.study.participants.annotationTypeView')
      })

      it('should go to the 404 state when an invalid eventTypeId is used', function() {
        const jsonAnnotType = this.Factory.annotationType(),
              jsonStudy     = this.Factory.study({ annotationTypes: [ jsonAnnotType ]}),
              study         = this.Study.create(jsonStudy)
        this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when(study))
        this.gotoUrl(`/admin/studies/study-id-1/participants/annottype/${this.Factory.stringNext()}`)
        expect(this.$state.current.name).toBe('404')
      })

    })

  describe('navigating to `/admin/studies/study-id-1/collection/events/event-type-id-1`', function () {

    beforeEach(function() {
      this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when(this.study))
    });

    it('should go to valid state', function () {
      const collectionEventType = this.CollectionEventType.create(this.Factory.collectionEventType())

      this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.when(collectionEventType))
      this.gotoUrl('/admin/studies/study-id-1/collection/events/event-type-id-1')
      expect(this.$state.current.name).toBe('home.admin.studies.study.collection.ceventType')
    })

    it('should go to the 404 state when an invalid eventId is used', function() {
      this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
      this.gotoUrl('/admin/studies/study-id-1/collection/events/event-type-id-1')
      expect(this.$state.current.name).toBe('404')
    })
  })

  it('when navigating to `/admin/studies/study-id-1/collection/add` should go to valid state',
     function () {
       this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when({}))
       this.gotoUrl('/admin/studies/study-id-1/collection/add')
       expect(this.$state.current.name).toBe('home.admin.studies.study.collection.ceventTypeAdd')
     })

  describe(
    'navigating to `/admin/studies/study-id-1/collection/events/event-type-id-1/annottypes/add`',
    function () {

      beforeEach(function() {
        this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when(this.study))
      });

      it('should go to valid state', function() {
        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.when({}))
        this.gotoUrl('/admin/studies/study-id-1/collection/events/event-type-id-1/annottypes/add')
        expect(this.$state.current.name)
          .toBe('home.admin.studies.study.collection.ceventType.annotationTypeAdd')
      })

      it('should go to the 404 state when an invalid eventId is used', function() {
        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
        this.gotoUrl('/admin/studies/study-id-1/collection/events/event-type-id-1/annottypes/add')
        expect(this.$state.current.name).toBe('404')
      })

    })

  describe(
    'navigating to `/admin/studies/study-id-1/collection/events/event-type-id-1/annottypes/annottype-id-1`',
    function () {

      beforeEach(function() {
        this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when(this.study))
      });

      it('should go to valid state', function() {
        const jsonAnnotType = this.Factory.annotationType(),
              jsonEventType = this.Factory.collectionEventType({ annotationTypes: [ jsonAnnotType ]}),
              eventType     = this.CollectionEventType.create(jsonEventType)

        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.when(eventType))
        this.gotoUrl(
          `/admin/studies/study-id-1/collection/events/event-type-id-1/annottypes/${jsonAnnotType.id}`)
        expect(this.$state.current.name)
          .toBe('home.admin.studies.study.collection.ceventType.annotationTypeView')
      })

      it('should go to the 404 state when an invalid eventId is used', function() {
        const jsonEventType = this.Factory.collectionEventType(),
              eventType     = this.CollectionEventType.create(jsonEventType)

        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.when(eventType))
        this.gotoUrl('/admin/studies/study-id-1/collection/events/event-type-id-1/annottypes/annottype-id-1')
        expect(this.$state.current.name).toBe('404')
      })

    })

  describe(
    'navigating to `/admin/studies/study-id-1/collection/events/event-type-id-1/spcdescs/add`',
    function () {

      beforeEach(function() {
        this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when(this.study))
      });

     it('should go to valid state', function() {
        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.when({}))
        this.gotoUrl('/admin/studies/study-id-1/collection/events/event-type-id-1/spcdescs/add')
        expect(this.$state.current.name)
          .toBe('home.admin.studies.study.collection.ceventType.specimenDescriptionAdd')
      })

      it('should go to the 404 state when an invalid eventId is used', function() {
        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
        this.gotoUrl('/admin/studies/study-id-1/collection/events/event-type-id-1/spcdescs/add')
        expect(this.$state.current.name).toBe('404')
      })

    })

  describe(
    'navigating to `/admin/studies/study-id-1/collection/events/event-type-id-1/spcdescs/spcedesc-id-1`',
    function () {

      beforeEach(function() {
        this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when(this.study))
      });

      it('should go to valid state', function() {
        const jsonSpcDesc   = this.Factory.collectionSpecimenDescription(),
              jsonEventType = this.Factory.collectionEventType({ specimenDescriptions: [ jsonSpcDesc ]}),
              eventType     = this.CollectionEventType.create(jsonEventType)

        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.when(eventType))
        this.gotoUrl(
          `/admin/studies/study-id-1/collection/events/event-type-id-1/spcdescs/${jsonSpcDesc.id}`)
        expect(this.$state.current.name)
          .toBe('home.admin.studies.study.collection.ceventType.specimenDescriptionView')
      })

      it('should go to the 404 state when an invalid eventId is used', function() {
        const jsonEventType = this.Factory.collectionEventType(),
              eventType     = this.CollectionEventType.create(jsonEventType)

        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.when(eventType))
        this.gotoUrl('/admin/studies/study-id-1/collection/events/event-type-id-1/spcdescs/spcedesc-id-1')
        expect(this.$state.current.name).toBe('404')
      })

    })

  function studySharedBehaviour(context) {

    describe('(shared)', function() {

      it('should go to the study`s child state', function() {
        this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when(this.study))
        this.gotoUrl(context.url)
        expect(this.$state.current.name).toBe(`home.admin.studies.study.${context.childState}`)
      })

      it('should go to the 404 state when an invalid studyId is used', function() {
        this.Study.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
        this.gotoUrl(context.url)
        expect(this.$state.current.name).toBe('404')
      })

    })
  }

})
