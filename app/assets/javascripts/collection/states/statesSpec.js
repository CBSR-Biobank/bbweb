/**
 * Jasmine test suite
 *
 */
/* global angular */

import ngModule from '../../app'  // the whole appliction has to be loaded for these states

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
                              'Participant',
                              'CollectionEventType',
                              'CollectionEvent',
                              'Specimen',
                              'Factory')

      this.initAuthentication()

      this.study = this.Study.create(this.Factory.study())
      this.participant = this.Participant.create(this.Factory.participant())
    })
  })

  it('when navigating to `/collection` should go to valid state', function () {
    this.gotoUrl('/collection')
    expect(this.$state.current.name).toBe('home.collection')
  })

  describe('when navigating to `/collection/study/study-slug-1`', function () {

    it('should go to valid state', function () {
      this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when({}))
      this.gotoUrl('/collection/study/study-slug-1')
      expect(this.$state.current.name).toBe('home.collection.study')
    })

    it('should go to the 404 state when an invalid studyId is used', function() {
      this.Study.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
      this.gotoUrl('/collection/study/study-slug-1')
      expect(this.$state.current.name).toBe('404')
    })

  })

  it('when navigating to `/collection/study/study-slug-1/add/participant-slug-1` should go to valid state',
     function () {
       this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when({}))
       this.gotoUrl('/collection/study/study-slug-1/add/participant-slug-1')
       expect(this.$state.current.name).toBe('home.collection.study.participantAdd')
     })

  describe(
    'when navigating to `/collection/study/study-slug-1/participants/participant-slug-1/summary`',
    function () {
      const context = {}

      beforeEach(function() {
        context.childState = 'summary'
        context.url = '/collection/study/study-slug-1/participants/participant-slug-1/summary'
      });

      participantSharedBehaviour(context)
    })

  describe(
    'when navigating to `/collection/study/study-slug-1/participants/participant-slug-1/events`',
    function () {
      const context = {}

      beforeEach(function() {
        context.childState = 'cevents'
        context.url = '/collection/study/study-slug-1/participants/participant-slug-1/events'
      });

      participantSharedBehaviour(context)
    })

  it('when navigating to `/collection/study/study-slug-1/participants/participant-slug-1/events/add` should go to valid state',
     function () {
       this.Study.get       = jasmine.createSpy().and.returnValue(this.$q.when(this.study))
       this.Participant.get = jasmine.createSpy().and.returnValue(this.$q.when(this.participant))

       this.gotoUrl('/collection/study/study-slug-1/participants/participant-slug-1/events/add')
       expect(this.$state.current.name).toBe('home.collection.study.participant.cevents.add')
     })

  describe(
    'when navigating to `/collection/study/study-slug-1/participants/participant-slug-1/events/add/collection-event-type-id-1`',
    function () {

      beforeEach(function() {
        this.Study.get       = jasmine.createSpy().and.returnValue(this.$q.when(this.study))
        this.Participant.get = jasmine.createSpy().and.returnValue(this.$q.when(this.participant))
      });

      it('should go to valid state', function() {
        const collectionEventType = this.CollectionEventType.create(this.Factory.collectionEventType())

        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.when(collectionEventType))
        this.gotoUrl(
          '/collection/study/study-slug-1/participants/participant-slug-1/events/add/collection-event-type-slug-1')
        expect(this.$state.current.name).toBe('home.collection.study.participant.cevents.add.details')
      })

      it('should go to the 404 state when an invalid eventTypeId is used', function() {
        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
        this.gotoUrl(
          '/collection/study/study-slug-1/participants/participant-slug-1/events/add/collection-event-type-id-1')
        expect(this.$state.current.name).toBe('404')
      })

    })

  describe(
    'navigating to `/collection/study/study-slug-1/participants/participant-slug-1/events/event-type-id-1/event-id-1`',
    function () {

      beforeEach(function() {
        const collectionEventType = this.CollectionEventType.create(this.Factory.collectionEventType())

        this.Study.get               = jasmine.createSpy().and.returnValue(this.$q.when(this.study))
        this.Participant.get         = jasmine.createSpy().and.returnValue(this.$q.when(this.participant))
        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.when(collectionEventType))
      });

      it('should go to valid state', function () {
        const collectionEvent = this.CollectionEvent.create(this.Factory.collectionEvent())

        this.CollectionEvent.getByVisitNumber = jasmine.createSpy().and.returnValue(this.$q.when(collectionEvent))

        this.gotoUrl(
          '/collection/study/study-slug-1/participants/participant-slug-1/events/event-id-1')
        expect(this.$state.current.name).toBe('home.collection.study.participant.cevents.details')
      })

      it('should go to the 404 state when an invalid eventId is used', function() {
        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
        this.gotoUrl(
          '/collection/study/study-slug-1/participants/participant-slug-1/events/add/event-type-id-1')
        expect(this.$state.current.name).toBe('404')
      })
    })

  describe(
    'when navigating to `/collection/study/study-slug-1/participants/participant-slug-1/events/event-id-1/spc/specimen-inv-id-1`',
    function () {

      beforeEach(function() {
        const collectionEventType = this.CollectionEventType.create(this.Factory.collectionEventType()),
              collectionEvent = this.CollectionEvent.create(this.Factory.collectionEvent())

        this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when(this.study))
        this.Participant.get = jasmine.createSpy().and.returnValue(this.$q.when(this.participant))
        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.when(collectionEventType))
        this.CollectionEvent.getByVisitNumber = jasmine.createSpy().and.returnValue(this.$q.when(collectionEvent))
      });

      it('should go to valid state', function() {
        const specimen = this.Specimen.create(this.Factory.specimen())

        this.Specimen.get = jasmine.createSpy().and.returnValue(this.$q.when(specimen))

        this.gotoUrl(
          '/collection/study/study-slug-1/participants/participant-slug-1/events/event-id-1/spc/specimen-inv-id-1')
        expect(this.$state.current.name).toBe('home.collection.study.participant.cevents.details.specimen')
      })

      it('should go to the 404 state when an invalid eventId is used', function() {
        this.Specimen.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
        this.gotoUrl(
          '/collection/study/study-slug-1/participants/participant-slug-1/events/event-id-1/spc/specimen-inv-id-1')
        expect(this.$state.current.name).toBe('404')
      })

    })

  function participantSharedBehaviour(context) {

    describe('(shared)', function() {

      beforeEach(function() {
        this.Study.get = jasmine.createSpy().and.returnValue(this.$q.when(this.study))
      });

      it('should go to the participant`s child state', function() {
        this.Participant.get = jasmine.createSpy().and.returnValue(this.$q.when(this.participant))
        this.gotoUrl(context.url)
        expect(this.$state.current.name).toBe(`home.collection.study.participant.${context.childState}`)
      })

      it('should go to the 404 state when an invalid participantId is used', function() {
        this.Participant.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
        this.gotoUrl(context.url)
        expect(this.$state.current.name).toBe('404')
      })

    })
  }

})
