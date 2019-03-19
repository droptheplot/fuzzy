package com.entities

import com.usecases.WhoisUsecase.{Raw, Status}

final case class SearchResponse(domain: Domain, status: Status, raw: Raw)
